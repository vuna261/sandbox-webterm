package com.cloudnut.webterm.websocket;

import com.cloudnut.webterm.application.services.interfaces.IWebTermService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import static com.cloudnut.webterm.application.services.impl.WebTermService.sessionClose;
import static com.cloudnut.webterm.utils.Constants.SESSION_UUID;

@Component
@Slf4j
public class WebTermHandler implements WebSocketHandler {
    @Autowired
    private IWebTermService webTermService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        webTermService.initSession(session);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        if (message instanceof TextMessage) {
            //logger.debug("Session: {}, command: {}", webSocketSession.getAttributes().get(SESSION_UUID), webSocketMessage.toString());
            log.trace("Session: {}, payload: {}", session.getAttributes().get(SESSION_UUID), message.getPayload());
            webTermService.textMessageHandler(((TextMessage) message).getPayload(), session);
        } else if (message instanceof BinaryMessage) {
            log.warn("NotHandled Binary WebSocket message: {}", message);
        } else if (message instanceof PingMessage) {
            log.warn("NotHandled Ping WebSocket message: {}", message);
        } else if (message instanceof PongMessage) {
            log.warn("NotHandled Pong WebSocket message: {}", message);
        } else {
            log.warn("Unexpected WebSocket message: {}", message);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("Data transport error: {}", exception.toString());

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        sessionClose(session);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
