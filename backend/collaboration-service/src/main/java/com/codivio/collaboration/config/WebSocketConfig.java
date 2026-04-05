package com.codivio.collaboration.config;

import com.codivio.collaboration.handler.CollaborationHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final CollaborationHandler collaborationHandler;

    public WebSocketConfig(CollaborationHandler collaborationHandler) {
        this.collaborationHandler = collaborationHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(collaborationHandler, "/collaboration/ws/{projectId}/{fileId}")
                .setAllowedOrigins("*");
    }
}
