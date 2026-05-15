package ch.usi.inf.bsc.sa4.lab02spring.controller;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.HeartbeatDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.HeartbeatResponseDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.Box;
import ch.usi.inf.bsc.sa4.lab02spring.model.GameObject;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.Position;
import ch.usi.inf.bsc.sa4.lab02spring.repository.LevelRepository;
import ch.usi.inf.bsc.sa4.lab02spring.service.antiCheat.AntiCheatSessionState;
import ch.usi.inf.bsc.sa4.lab02spring.service.antiCheat.HeartbeatValidator;
import ch.usi.inf.bsc.sa4.lab02spring.service.antiCheat.ViolationCode;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/// WebSocket endpoint for anti-cheat heartbeats.
@Component
public class AntiCheatWebSocketHandler extends TextWebSocketHandler {

    private static final String USER_ID_ATTRIBUTE = "userId";
    private static final String INITIAL_TIME_ATTRIBUTE = "startTime";
    private static final String VALIDATE_FRAME_ATTRIBUTE = "frameCount";
    private static final String FRAME_MISMATCH_LOGGED_ATTRIBUTE = "frameMismatchLogged";
    private static final String HEARTBEAT_TIMEOUT_LOGGED_ATTRIBUTE = "heartbeatTimeoutLogged";
    /// First frame expected from a new socket.
    private static final int VALIDATE_FRAME_DEFAULT = 1;
    /// Allowed delay beyond the 60 FPS schedule.
    private static final long MAX_ACCEPTED_NETWORK_DELAY = 500;
    /// Small buffer to avoid logging timer jitter.
    private static final long TIMEOUT_LOG_JITTER = 50;
    /// Same fixed 60 FPS cadence used by the frontend.
    private static final double FRAME_DELTA = 16.67;

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
        final WebSocketSession prev = activeSockets.put(userId, session);
        if (prev != null) {
            prev.close(CloseStatus.NORMAL.withReason("Replaced by a newer anti-cheat session"));
        }

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
        // Support means surfaces the player can actually stand on.
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

        // Frame 1 starts the run clock, not the WebSocket open time.
        final long effectiveStartTime = heartbeat.frame() == 1 ? now : startTime.orElseThrow();
        if (heartbeat.frame() == 1) {
            session.getAttributes().put(INITIAL_TIME_ATTRIBUTE, effectiveStartTime);
        }

        final List<ViolationCode> cadenceViolations = new ArrayList<>();
        final long maxReceiveTime = (long) (effectiveStartTime + (heartbeat.frame() * FRAME_DELTA) + MAX_ACCEPTED_NETWORK_DELAY);
        if (heartbeat.frame() != frameCount.orElseThrow()) {
            if (!readBooleanAttribute(session, FRAME_MISMATCH_LOGGED_ATTRIBUTE)) {
                log.warn("Expected frame mismatch. got={} expected={} UserId={}",
                        heartbeat.frame(), frameCount.orElseThrow(), userId.orElseThrow());
                session.getAttributes().put(FRAME_MISMATCH_LOGGED_ATTRIBUTE, Boolean.TRUE);
                cadenceViolations.add(ViolationCode.FRAME_MISMATCH);
            }
        } else if (now > maxReceiveTime + TIMEOUT_LOG_JITTER) {
            if (!readBooleanAttribute(session, HEARTBEAT_TIMEOUT_LOGGED_ATTRIBUTE)) {
                log.warn("Network request timeout. UserId={} frame={} delayMs={}",
                        userId.orElseThrow(), heartbeat.frame(), now - maxReceiveTime);
                session.getAttributes().put(HEARTBEAT_TIMEOUT_LOGGED_ATTRIBUTE, Boolean.TRUE);
                cadenceViolations.add(ViolationCode.HEARTBEAT_TIMEOUT);
            }
        }

        final AntiCheatSessionState state = sessions.get(userId.orElseThrow());
        if (state == null) {
            log.warn("Unknown anticheat userId");
            session.close(CloseStatus.NOT_ACCEPTABLE);
            return;
        }

        final HeartbeatResponseDTO validationResponse = validator.validate(heartbeat, state);
        if (!validationResponse.violations().isEmpty()) {
            log.warn("Anticheat violation userId={} frame={} violations={}",
                    userId.orElseThrow(), heartbeat.frame(), validationResponse.violations());
        }
        final List<ViolationCode> violations = new ArrayList<>(cadenceViolations);
        violations.addAll(validationResponse.violations());
        session.getAttributes().put(VALIDATE_FRAME_ATTRIBUTE, heartbeat.frame() + 1);
        writeMessage(session, new HeartbeatResponseDTO(heartbeat.frame(), violations));
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

    private boolean readBooleanAttribute(final WebSocketSession session, final String key) {
        return Boolean.TRUE.equals(session.getAttributes().get(key));
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
