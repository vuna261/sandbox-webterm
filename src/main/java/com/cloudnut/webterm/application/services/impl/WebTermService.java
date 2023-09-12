package com.cloudnut.webterm.application.services.impl;

import com.cloudnut.webterm.application.services.interfaces.ITokenRequestService;
import com.cloudnut.webterm.application.services.interfaces.IWebTermService;
import com.cloudnut.webterm.thirdparty.connection.Connection;
import com.cloudnut.webterm.thirdparty.connection.impl.PtyConnection;
import com.cloudnut.webterm.thirdparty.pojo.TerminalSessionInfo;
import com.cloudnut.webterm.thirdparty.pojo.TokenRequest;
import com.cloudnut.webterm.thirdparty.pojo.TokenRowCol;
import com.cloudnut.webterm.thirdparty.pojo.TwoWayMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcraft.jsch.JSchException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.cloudnut.webterm.thirdparty.Utils.MessageUtils.toClientString;
import static com.cloudnut.webterm.utils.Constants.*;

@Service
@Slf4j
public class WebTermService implements IWebTermService {
    private static final Map<String, Connection> SESSION_MAP = new ConcurrentHashMap<>();
    private static final ExecutorService executorService = Executors.newCachedThreadPool();

    private static final ObjectMapper om = new ObjectMapper();

    @Autowired
    ITokenRequestService tokenRequestService;

    /**
     * initial session
     * @param session
     */
    @Override
    public void initSession(WebSocketSession session) {
        String sessionId = UUID.randomUUID().toString().replace("-","");
        session.getAttributes().put(SESSION_UUID, sessionId);
    }

    /**
     * handler text message
     * @param buffer
     * @param session
     */
    @Override
    public void textMessageHandler(String buffer, WebSocketSession session) {
        TwoWayMessage clientMsg;
        try {
            clientMsg = om.readValue(buffer, TwoWayMessage.class);
        } catch (IOException ex) {
            sendOOBMessage(session, ex);
            sessionClose(session);
            return;
        }
        String sessionId = String.valueOf(session.getAttributes().get(SESSION_UUID));
        if (CLIENT_CONNECT.equalsIgnoreCase(clientMsg.getType())) {
            TokenRowCol tokenRowCol;
            try {
                tokenRowCol = om.readValue(clientMsg.getPayload(), TokenRowCol.class);
            } catch (IOException ex) {
                sendOOBMessage(session, ex);
                sessionClose(session);
                return;
            }
            TokenRequest req = tokenRequestService.tokenToRequest(tokenRowCol.getToken());
            if (req != null) {
                TerminalSessionInfo terminalSessionInfo = TerminalSessionInfo.from(req);
                final Connection connection = new PtyConnection(session, terminalSessionInfo);
                SESSION_MAP.put(sessionId, connection);
                terminalSessionInfo.setSessionId(sessionId);

                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            connection.connect();
                        } catch (JSchException | IOException | InterruptedException ex) {
                            sendCauseMessage(connection, ex);
                            sessionClose(session);
                        }
                    }
                });
            } else {
                log.error("Token not found: {}", tokenRowCol.getToken());
                sendOOBMessage(session, "Session Token not found");
                sessionClose(session);
            }
        } else if (CLIENT_DATA.equalsIgnoreCase(clientMsg.getType())) {
            String payload = clientMsg.getPayload();
            Connection connection = SESSION_MAP.get(sessionId);
            if (connection != null) {
                try {
                    if (connection.getTerminalSessionInfo().isReady()) {
                        connection.send(payload);
                    } else {
                        log.info("Session is not ready");
                    }
                } catch (IOException ex) {
                    sendCauseMessage(connection, ex);
                    sessionClose(session);
                }
            } else {
                sendOOBMessage(session, "Unknown Session for data");
                sessionClose(session);
            }
        } else if (CLIENT_DISCONNECT.equalsIgnoreCase(clientMsg.getType())) {
            Connection connection = tokenToConnection(clientMsg.getPayload());
            if (connection != null && connection.getTerminalSessionInfo().getSessionId().equals(sessionId)) {
                sendOOBMessage(session, "Session disconnecting");
                connectionClose(connection);
            } else {
                sendOOBMessage(session, "Unknown Session for Disconnect");
                sessionClose(session);
            }
        } else {
            sendOOBMessage(session, "Unsupported Message: " + clientMsg);
            sessionClose(session);
        }
    }

    @Override
    public List<TerminalSessionInfo> getTerminalSessionInfo(String token) {
        return null;
    }

    /**
     * drop session
     * @param token
     * @return
     */
    @Override
    public Map dropSession(String token) {
        Connection connection = null;
        Iterator<Map.Entry<String, Connection>> iterator = SESSION_MAP.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Connection> entry = iterator.next();
            connection = entry.getValue();
            if (token.equals(connection.getTerminalSessionInfo().getToken())) {
                break;
            } else {
                connection = null;
            }
        }
        if (connection != null) {
            try {
                connection.sendToUser("\r\nSession Terminated Administratively");
            } catch (IOException ex) {
            }

            connectionClose(connection);

            return Collections.singletonMap("status", "SUCCESS: " + token + " REMOVED");
        } else {
            return Collections.singletonMap("status", "FAILURE: " + token + " NOT FOUND");
        }
    }

    /**
     * send error message
     * @param session
     * @param throwable
     */
    private static void sendOOBMessage(WebSocketSession session, Throwable throwable) {
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(toClientString(CR_LF + throwable.getCause().getMessage() + CR_LF));
            } catch (IOException ex) {

            }
        }
    }

    /**
     *
     * @param session
     * @param message
     */
    private static void sendOOBMessage(WebSocketSession session, String message) {
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(toClientString(CR_LF + message + CR_LF));
            } catch (IOException ex) {
            }
        }
    }

    /**
     * send cause message
     * @param connection
     * @param throwable
     */
    private static void sendCauseMessage(Connection connection, Throwable throwable) {
        if (connection != null) {
            try {
                connection.sendToUser(CR_LF + throwable.getCause().getMessage() + CR_LF);
            } catch (IOException ex) {}
        }
    }

    /**
     *
     * @param sessionId
     */
    public static void removeFromSessionMap(String sessionId) {
        SESSION_MAP.remove(sessionId);
    }


    /**
     * close session
     * @param session
     */
    public static void sessionClose(WebSocketSession session) {
        String sessionId = String.valueOf(session.getAttributes().get(SESSION_UUID));
        Connection connection = SESSION_MAP.get(sessionId);
        if (connection != null) {
            try {
                // get related session
                ArrayList<String> childSession = new ArrayList<>();
                if (childSession != null) {
                    for (String child: childSession) {
                        Connection con = SESSION_MAP.get(child);
                        if (con != null) {
                            con.sendToUser("\r\nDisconnecting(Session Closed)\r\n");
                            con.close();
                            con.webSocketSessionClose();
                            SESSION_MAP.remove(child);
                        }
                    }
                }
                connection.close();
                connection.webSocketSessionClose();
            } catch (IOException e) {
                log.error("Session close exception: {}", e.getMessage());
            }
            SESSION_MAP.remove(sessionId);
        } else {
            try {
                session.close();
            } catch (IOException ex) {

            }
        }
    }

    /**
     * close connection and related connection
     * @param connection
     */
    public void connectionClose(Connection connection) {
        String sessionId = connection.getTerminalSessionInfo().getSessionId();
        String token = connection.getTerminalSessionInfo().getToken();
        try {
            List<String> childToken = tokenRequestService.getChildToken(token);
            if (childToken != null) {
                for (String child : childToken) {
                    Connection childConn = tokenToConnection(child);
                    if (childConn != null) {
                        String childSessionId = childConn.getTerminalSessionInfo().getSessionId();
                        childConn.sendToUser("\r\nDisconnecting(Parent Session Closed)\r\n");
                        childConn.close();
                        childConn.webSocketSessionClose();
                        SESSION_MAP.remove(childSessionId);
                    }
                }
                connection.close();
                connection.webSocketSessionClose();
            }
        } catch (IOException ex) {
        }
        SESSION_MAP.remove(sessionId);
    }

    /**
     * Get connection from token
     * @param token
     * @return
     */
    public static Connection tokenToConnection(String token) {
        Iterator<Map.Entry<String, Connection>> iterator = SESSION_MAP.entrySet().iterator();

        while (token != null && iterator.hasNext()) {
            Map.Entry<String, Connection> entry = iterator.next();
            Connection connection = entry.getValue();
            if (connection.getTerminalSessionInfo().getToken().equals(token)) {
                return connection;
            }
        }
        return null;
    }

    /**
     * get Connection from token
     * @param token
     * @return
     */
    public static Connection tokenToRootConnection(String token) {
        Iterator<Map.Entry<String, Connection>> iterator = SESSION_MAP.entrySet().iterator();

        while (token != null && iterator.hasNext()) {
            Map.Entry<String, Connection> entry = iterator.next();
            Connection connection = entry.getValue();
            if (connection.getTerminalSessionInfo().getToken().equals(token)) {
                if (!connection.getTerminalSessionInfo().getSessionType().equalsIgnoreCase(NEW)
                        && connection.getTerminalSessionInfo().getParentToken() != null) {
                    return tokenToRootConnection(connection.getTerminalSessionInfo().getParentToken());
                }
                return connection;
            }
        }
        return null;
    }

    // session house keeping
    @Scheduled(fixedDelay = 10000, initialDelay = 10000)
    public void sessionHouseKeeping() {
        ArrayList<Connection> candidates = new ArrayList<>();
        Iterator<Map.Entry<String, Connection>> iterator = SESSION_MAP.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Connection> entry = iterator.next();
            Connection connection = entry.getValue();
            if (connection.getTerminalSessionInfo().getExpiredTime().before(new Date())) {
                candidates.add(connection);
            }
        }
        for (Connection connection: candidates) {
            try {
                connection.sendToUser("\r\nSession Timed Out");
            } catch (IOException ex) {

            }
            connectionClose(connection);
        }
    }
}
