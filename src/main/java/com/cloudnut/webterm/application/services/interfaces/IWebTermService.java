package com.cloudnut.webterm.application.services.interfaces;

import com.cloudnut.webterm.thirdparty.pojo.TerminalSessionInfo;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.Map;

public interface IWebTermService {
    void initSession(WebSocketSession session);
    void textMessageHandler(String buffer, WebSocketSession session);
    List<TerminalSessionInfo> getTerminalSessionInfo(String token);
    Map dropSession(String token);
}
