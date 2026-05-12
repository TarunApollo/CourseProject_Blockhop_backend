package ch.usi.inf.bsc.sa4.lab02spring.controller;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.HeartbeatDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.HeartbeatErrorResponseDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.HeartbeatResponseDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.Box;
import ch.usi.inf.bsc.sa4.lab02spring.model.GameObject;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.Position;
import ch.usi.inf.bsc.sa4.lab02spring.repository.LevelRepository;
import ch.usi.inf.bsc.sa4.lab02spring.service.antiCheat.AntiCheatSessionState;
import ch.usi.inf.bsc.sa4.lab02spring.service.antiCheat.HeartbeatValidator;
import ch.usi.inf.bsc.sa4.lab02spring.utils.AuthUtils;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/// Anticheat heartbeat endpoint.
///
/// Raw Spring WebSocket handler shape based on:
/// https://docs.spring.io/spring-framework/reference/web/websocket/server.html
///
/// TextWebSocketHandler lifecycle and text only behavior reference:
/// https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/socket/handler/TextWebSocketHandler.html
///
/// Similar raw TextWebSocketHandler example using afterConnectionEstablished and
/// in memory (for now) session
/// tracking: https://github.com/eugenp/tutorials/blob/master/webrtc/src/main/java/com/baeldung/webrtc/SocketHandler.java
@Component
public class AntiCheatWebSocketHandler extends TextWebSocketHandler {

    private static final String USER_ID_ATTRIBUTE = "userId";
    private static final String INITIAL_TIME_ATTRIBUTE = "startTime";
    private static final String VALIDATE_FRAME_ATTRIBUTE = "frameCount";
    private static final String PARALLEL_SESSION_MESSAGE =
            "This run won't count for leaderboard because another session is currently running.";

    private static final int VALIDATE_FRAME_DEFAULT = 1;
    private static final long MAX_ACCEPTED_NETWORK_DELAY = 200;
    private static final double FRAME_DELTA = 16.67; // 60 FPS

    private static final Logger log = LoggerFactory.getLogger(AntiCheatWebSocketHandler.class);

    private final HeartbeatValidator validator;
    private final LevelRepository levelRepository;
    private final Map<String, AntiCheatSessionState> sessions = new ConcurrentHashMap<>();
    private final Map<String, WebSocketSession> activeSockets = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AntiCheatWebSocketHandler(final HeartbeatValidator validator, final LevelRepository levelRepository) {
        this.validator = validator;
        this.levelRepository = levelRepository;
    }

    @Override
    public void afterConnectionEstablished(final WebSocketSession session) throws IOException {
        final long now = System.currentTimeMillis();
        final Authentication authentication = getAuthentication(session);
        if (authentication == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Unauthenticated"));
            return;
        }

        final Optional<Level> level = levelRepository.findById(readLevelId(session));
        if (level.isEmpty()) {
            log.warn("Unknown anti-cheat level path: {}", session.getUri());
            session.close(CloseStatus.NOT_ACCEPTABLE);
            return;
        }

        final String userId = AuthUtils.getUserIdFromAuth(authentication);
        final WebSocketSession existingSession = activeSockets.get(userId);
        // TODO: Move away from null.
        if (existingSession != null && existingSession.isOpen()) {
            sendErrorMessage(session, new HeartbeatErrorResponseDTO(
                PARALLEL_SESSION_MESSAGE,
                null,
                null,
                null,
                null
            ));
            session.close(CloseStatus.POLICY_VIOLATION.withReason("Parallel anticheat sessions are not allowed"));
            return;
        }

        if (existingSession != null) {
            activeSockets.remove(userId, existingSession);
            sessions.remove(userId);
        }

        activeSockets.put(userId, session);
        sessions.put(userId, new AntiCheatSessionState(supportTiles(level.orElseThrow())));
        session.getAttributes().put(INITIAL_TIME_ATTRIBUTE, now);
        session.getAttributes().put(USER_ID_ATTRIBUTE, userId);
        session.getAttributes().put(VALIDATE_FRAME_ATTRIBUTE, VALIDATE_FRAME_DEFAULT);
        writeMessage(session, new HeartbeatResponseDTO(0, List.of()));
    }

    private static String readLevelId(final WebSocketSession session) {
        final String path = session.getUri() == null ? "" : session.getUri().getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }

    private static Set<Position> supportTiles(final Level level) {
        final Set<Position> support = level.getWorldLayer().keySet().stream().collect(Collectors.toSet());
        level.getObjectLayer().values().stream()
                .filter(Box.class::isInstance)
                .map(GameObject::pos)
                .forEach(support::add);
        return support;
    }

    @Override
    public void afterConnectionClosed(final WebSocketSession session, final CloseStatus status) {
        final Object userId = session.getAttributes().get(USER_ID_ATTRIBUTE);
        if (userId instanceof String id) {
            activeSockets.remove(id, session);
            if (!activeSockets.containsKey(id)) {
                sessions.remove(id);
            }
        }
    }

    @Override
    protected void handleTextMessage(final WebSocketSession session, final TextMessage message)
            throws IOException {
        final long now = System.currentTimeMillis();
        final Optional<HeartbeatDTO> payload = readHeartbeat(session, message);
        if (payload.isEmpty()) {
            return;
        }

        final HeartbeatDTO heartbeat = payload.orElseThrow();
        final Optional<String> userId = readStringAttribute(session, USER_ID_ATTRIBUTE);
        final Optional<Integer> frameCount = readIntegerAttribute(session, VALIDATE_FRAME_ATTRIBUTE);
        final Optional<Long> startTime = readLongAttribute(session, INITIAL_TIME_ATTRIBUTE);
        if (userId.isEmpty() || frameCount.isEmpty() || startTime.isEmpty()) {
            log.warn("Missing anti-cheat session attributes");
            session.close(CloseStatus.SERVER_ERROR);
            return;
        }

        final long maxRecieveTime = (long) (startTime.orElseThrow() + (heartbeat.frame() * FRAME_DELTA) + MAX_ACCEPTED_NETWORK_DELAY);
        if (heartbeat.frame() != frameCount.orElseThrow()) {
            final String errorMessage = "Expected frame mismatch";
            log.warn("{}. UserId={} is an invalid attempt.", errorMessage, userId.orElseThrow());
            final HeartbeatErrorResponseDTO error = new HeartbeatErrorResponseDTO(
                errorMessage,
                frameCount.orElseThrow(),
                heartbeat.frame(),
                null,
                null
            );
            sendErrorMessage(session, error);
            return;
        } else if (now > maxRecieveTime) {
            final String errorMessage = "Network request timeout, network is unstable";
            log.warn("{}. UserId={} is an invalid attempt.", errorMessage, userId.orElseThrow());
            final HeartbeatErrorResponseDTO error = new HeartbeatErrorResponseDTO(
                errorMessage,
                null,
                null,
                maxRecieveTime,
                now
            );
            sendErrorMessage(session, error);
            return;
        }

        final AntiCheatSessionState state = sessions.get(userId.orElseThrow());
        if (state == null) {
            log.warn("Unknown anti-cheat userId");
            session.close(CloseStatus.NOT_ACCEPTABLE);
            return;
        }

        final HeartbeatResponseDTO response = validator.validate(heartbeat, state);
        if (!response.violations().isEmpty()) {
            log.warn("Anti-cheat violation userId={} frame={} violations={}",
                    userId.orElseThrow(), heartbeat.frame(), response.violations());
        }
        session.getAttributes().put(VALIDATE_FRAME_ATTRIBUTE, heartbeat.frame() + 1);
        writeMessage(session, response);
    }

    private Optional<String> readStringAttribute(final WebSocketSession session, final String key) {
        final Object attribute = session.getAttributes().get(key);
        if (attribute instanceof String value) {
            return Optional.of(value);
        }
        return Optional.empty();
    }

    private Optional<Integer> readIntegerAttribute(final WebSocketSession session, final String key) {
        final Object attribute = session.getAttributes().get(key);
        if (attribute instanceof Integer value) {
            return Optional.of(value);
        }
        return Optional.empty();
    }

    private Optional<Long> readLongAttribute(final WebSocketSession session, final String key) {
        final Object attribute = session.getAttributes().get(key);
        if (attribute instanceof Long value) {
            return Optional.of(value);
        }
        return Optional.empty();
    }

    private Optional<HeartbeatDTO> readHeartbeat(final WebSocketSession session, final TextMessage message)
            throws IOException {
        try {
            return Optional.of(objectMapper.readValue(message.getPayload(), HeartbeatDTO.class));
        } catch (final Exception e) {
            log.warn("Failed to parse heartbeat payload", e);
            session.close(CloseStatus.BAD_DATA);
            return Optional.empty();
        }
    }

    private void sendErrorMessage(final WebSocketSession session, final HeartbeatErrorResponseDTO error) throws IOException {
        session.sendMessage(new TextMessage(objectMapper.writeValueAsBytes(error)));
    }

    private void writeMessage(final WebSocketSession session, final HeartbeatResponseDTO response) throws IOException {
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
    }

    @Override
    public void handleTransportError(final WebSocketSession session, final Throwable exception) {
        log.warn("WebSocket transport error");
    }

    private @Nullable Authentication getAuthentication(WebSocketSession session) {
        final Principal principal = session.getPrincipal();
        if (principal instanceof Authentication authentication) {
            return authentication;
        }

        return null;
    }
}
