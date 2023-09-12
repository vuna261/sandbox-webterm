package com.cloudnut.webterm.thirdparty.connection;

import com.cloudnut.webterm.thirdparty.Utils.MessageUtils;
import com.cloudnut.webterm.thirdparty.pojo.TerminalSessionInfo;
import com.jcraft.jsch.JSchException;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public abstract class Connection {
    /**
     * WebsocketSession of this connection
     */
    protected volatile WebSocketSession webSocketSession;

    /**
     * TerminalSessionInfo of this connection
     */
    protected final TerminalSessionInfo terminalSessionInfo;

    /**
     * constructor
     * @param webSocketSession
     * @param terminalSessionInfo
     */
    public Connection(WebSocketSession webSocketSession, TerminalSessionInfo terminalSessionInfo) {
        this.webSocketSession = webSocketSession;
        this.terminalSessionInfo = terminalSessionInfo;
    }

    /**
     * start connection on separate thread
     * @throws JSchException
     * @throws IOException
     * @throws InterruptedException
     */
    public abstract void connect() throws JSchException, IOException, InterruptedException;

    /**
     * send data to downstream
     * @param data
     * @throws IOException
     */
    public abstract void send(String data) throws IOException;

    /**
     * check
     * @return
     */
    public abstract boolean isAlive();

    /**
     * send message to upstream user
     * @param message
     * @throws IOException
     */
    public void sendToUser(byte[] message) throws IOException {
        this.sendToUser(MessageUtils.toClientBytes(message));
    }

    /**
     * send string message to upstream user
     * @param message
     * @throws IOException
     */
    public void sendToUser(String message) throws IOException {
        this.sendToUser(MessageUtils.toClientString(message));
    }

    /**
     * send binary message to user
     * @param message
     * @throws IOException
     */
    public void sendToUser(BinaryMessage message) throws IOException {
        if (webSocketSession != null && webSocketSession.isOpen()) {
            synchronized (webSocketSession) {
                webSocketSession.sendMessage(message);
            }
            terminalSessionInfo.setTrafficTimeNow();
        }
    }

    /**
     * Send text message to user
     * @param message
     * @throws IOException
     */
    public void sendToUser(TextMessage message) throws IOException {
        if (webSocketSession != null && webSocketSession.isOpen()) {
            synchronized (webSocketSession) {
                webSocketSession.sendMessage(message);
            }
            terminalSessionInfo.setTrafficTimeNow();
        }
        // send to other session
        // T.B.D
    }

    public TerminalSessionInfo getTerminalSessionInfo() {
        return terminalSessionInfo;
    }

    public WebSocketSession getWebSocketSession() {
        return webSocketSession;
    }

    public void setWebSocketSession(WebSocketSession webSocketSession) {
        this.webSocketSession = webSocketSession;
    }

    /**
     * close connection
     */
    public void webSocketSessionClose() {
        if (webSocketSession != null && webSocketSession.isOpen()) {
            try {
                webSocketSession.close();
            } catch (Exception e) {
                // do nothing
            }
        }
    }

    /**
     * close connection
     */
    public abstract void close();

    /**
     * screen resize
     */
    public abstract void resize();

    public void blockingRead(InputStream in) throws IOException {
        // wipe password now
        if (terminalSessionInfo.getPassword() != null) {
            terminalSessionInfo.setPassword("*");
        }
        terminalSessionInfo.setReady(true);

        try {
            byte[] buffer = new byte[1024];
            int i;
            while ((i = in.read(buffer)) != -1) {
                byte[] bi = Arrays.copyOfRange(buffer, 0, i);
                sendToUser(bi);
                terminalSessionInfo.setTrafficTimeNow();
            }
        } finally {
            sendToUser("Server Closed Connection");
            // connectionClose(this);
        }
    }
}