package com.cloudnut.webterm.api.rest.configuration;

import com.cloudnut.webterm.websocket.WebTermHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebTermSocketConfig implements WebSocketConfigurer {
    @Autowired
    WebTermHandler webTermHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webTermHandler, "/webterminal")
                .setAllowedOrigins("*");
    }
}
