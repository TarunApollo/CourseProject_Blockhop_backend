package ch.usi.inf.bsc.sa4.lab02spring.configuration;

import ch.usi.inf.bsc.sa4.lab02spring.controller.AntiCheatWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/// anticheat websocket endpoint
@Configuration
@EnableWebSocket
@SuppressWarnings("PMD.AtLeastOneConstructor")
public class WebSocketConfig implements WebSocketConfigurer {

    private final AntiCheatWebSocketHandler antiCheatHandler;

    public WebSocketConfig(final AntiCheatWebSocketHandler antiCheatHandler) {
        this.antiCheatHandler = antiCheatHandler;
    }

    @Override
    public void registerWebSocketHandlers(final WebSocketHandlerRegistry registry) {
        registry.addHandler(antiCheatHandler, "/ws/anti-cheat/{levelId}")
                .setAllowedOrigins("http://localhost:3000");
    }
}
