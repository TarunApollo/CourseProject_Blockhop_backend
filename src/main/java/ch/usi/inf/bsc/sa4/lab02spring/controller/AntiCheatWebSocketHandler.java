package ch.usi.inf.bsc.sa4.lab02spring.controller;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.HeartbeatDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.HeartbeatResponseDTO;
import ch.usi.inf.bsc.sa4.lab02spring.service.antiCheat.AntiCheatSessionState;
import ch.usi.inf.bsc.sa4.lab02spring.service.antiCheat.HeartbeatValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/// Anticheat heartbeat endpoint.
///
/// Raw Spring WebSocket handler shape based on:
/// https://docs.spring.io/spring-framework/reference/web/websocket/server.html
///
/// TextWebSocketHandler lifecycle and text only behavior reference:
/// https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/socket/handler/TextWebSocketHandler.html
///
/// Similar raw TextWebSocketHandler example using
/// afterConnectionEstablished and in memory (for now) session tracking:
/// https://github.com/eugenp/tutorials/blob/master/webrtc/src/main/java/com/baeldung/webrtc/SocketHandler.java
@Component
public class AntiCheatWebSocketHandler extends TextWebSocketHandler {

    private static final String RUN_ID_ATTRIBUTE = "antiCheatRunId";

    private static final Logger log = LoggerFactory.getLogger(AntiCheatWebSocketHandler.class);

    private final HeartbeatValidator validator;
    private final Map<String, AntiCheatSessionState> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AntiCheatWebSocketHandler(final HeartbeatValidator validator) {
        this.validator = validator;
    }

    @Override
    public void afterConnectionEstablished(final WebSocketSession session) throws IOException {
        final String runId = UUID.randomUUID().toString();
        sessions.put(runId, new AntiCheatSessionState());
        session.getAttributes().put(RUN_ID_ATTRIBUTE, runId);
        writeMessage(session, new HeartbeatResponseDTO(runId, 0, List.of()));
    }

    @Override
    public void afterConnectionClosed(final WebSocketSession session, final CloseStatus status) {
        final Object runId = session.getAttributes().get(RUN_ID_ATTRIBUTE);
        if (runId instanceof String id) {
            sessions.remove(id);
        }
    }

    @Override
    protected void handleTextMessage(final WebSocketSession session, final TextMessage message)
            throws IOException {
        final Optional<HeartbeatDTO> payload = readHeartbeat(session, message);
        if (payload.isEmpty()) {
            return;
        }

        final HeartbeatDTO heartbeat = payload.orElseThrow();
        final AntiCheatSessionState state = sessions.get(heartbeat.runId());
        if (state == null) {
            log.warn("Unknown runID");
            session.close(CloseStatus.NOT_ACCEPTABLE);
            return;
        }

        final HeartbeatResponseDTO response = validator.validate(heartbeat, state);
        if (!response.violations().isEmpty()) {
            log.warn("Anti-cheat violation runId={} frame={} violations={}",
                    heartbeat.runId(), heartbeat.frame(), response.violations());
        }
        writeMessage(session, response);
    }

    private Optional<HeartbeatDTO> readHeartbeat(final WebSocketSession session, final TextMessage message) throws IOException {
        try {
            return Optional.of(objectMapper.readValue(message.getPayload(), HeartbeatDTO.class));
        } catch (final RuntimeException e) {
            log.warn("Failed to parse heartbeat payload");
            session.close(CloseStatus.BAD_DATA);
            return Optional.empty();
        }
    }

    private void writeMessage(final WebSocketSession session, final HeartbeatResponseDTO response) throws IOException {
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
    }

    @Override
    public void handleTransportError(final WebSocketSession session, final Throwable exception) {
        log.warn("WebSocket transport error");
    }
}
