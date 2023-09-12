package com.cloudnut.webterm.thirdparty.connection.impl;

import com.cloudnut.webterm.thirdparty.connection.Connection;
import com.cloudnut.webterm.thirdparty.pojo.TerminalSessionInfo;
import com.jcraft.jsch.JSchException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

import static com.cloudnut.webterm.application.services.impl.WebTermService.removeFromSessionMap;
import static com.cloudnut.webterm.application.services.impl.WebTermService.tokenToRootConnection;

@Slf4j
public class JoinConnection extends Connection {

    private final Connection parrentConn;

    public JoinConnection(WebSocketSession session, TerminalSessionInfo terminalSessionInfo) {
        super(session, terminalSessionInfo);

        // could be null
        this.parrentConn = tokenToRootConnection(terminalSessionInfo.getParentToken());
    }

    /**
     * start connection on separate thread
     *
     * @throws JSchException
     * @throws IOException
     * @throws InterruptedException
     */
    @Override
    public void connect() throws JSchException, IOException, InterruptedException {
        if (parrentConn != null && parrentConn.isAlive()) {
            TerminalSessionInfo parentTer = parrentConn.getTerminalSessionInfo();
            terminalSessionInfo.setHost(parentTer.getHost());
            terminalSessionInfo.setPort(parentTer.getPort());
            terminalSessionInfo.setExpiredTime(parentTer.getExpiredTime());
            terminalSessionInfo.setParentToken(StringUtils.isEmpty(parentTer.getParentToken()) ?
                    parentTer.getToken() : parentTer.getParentToken());
            terminalSessionInfo.setReady(true);
        } else {
            sendToUser("Session No Longer Available");
        }
    }

    /**
     * send data to downstream
     *
     * @param data
     * @throws IOException
     */
    @Override
    public void send(String data) throws IOException {
        if (parrentConn != null) {
            parrentConn.send(data);
        }
    }

    /**
     * check
     *
     * @return
     */
    @Override
    public boolean isAlive() {
        return parrentConn.isAlive();
    }

    /**
     * close connection
     */
    @Override
    public synchronized void close() {
        removeFromSessionMap(terminalSessionInfo.getSessionId());
    }

    /**
     * screen resize
     */
    @Override
    public void resize() {
    }
}
