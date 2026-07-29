package com.street.combat.config;

import com.street.combat.Decisao;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSockConfig implements WebSocketConfigurer {

    private final Decisao decisao;

    public WebSockConfig(Decisao decisao) {
        this.decisao = decisao;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(decisao, "/jogo")
                .setAllowedOrigins("*");
    }
}
