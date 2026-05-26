package ch.usi.inf.bsc.sa4.lab02spring.service.anticheat;

import ch.usi.inf.bsc.sa4.lab02spring.controller.ReplaySerializationException;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.ReplayRequestDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.ReplayResultDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.AttemptVerificationStatus;
import ch.usi.inf.bsc.sa4.lab02spring.repository.LevelRepository;
import ch.usi.inf.bsc.sa4.lab02spring.service.AttemptService;
import ch.usi.inf.bsc.sa4.lab02spring.service.TileSetService;
import ch.usi.inf.bsc.sa4.lab02spring.service.UserService;
import ch.usi.inf.bsc.sa4.lab02spring.utils.ForbiddenUserException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.LevelNotFoundException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.UserNotFoundException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.anticheat.InputLogFingerprintUtils;
import ch.usi.inf.bsc.sa4.lab02spring.utils.converter.LayerToTiledMapConverter;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

/// Handles replay submission orchestration outside the HTTP controller.
@SuppressFBWarnings(
        value = "EI_EXPOSE_REP2",
        justification = "Spring injects shared collaborators that stay private to this service."
)
@Service
public class ReplaySubmissionService {

    /// Replay frame jitter tolerated before flagging a mismatch.
    private static final int FRAME_TOLERANCE = 5;

    /// Service that executes the frontend replay process.
    private final ReplayService replayService;

    /// Service that loads attempts and persists anti-cheat results.
    private final AttemptService attemptService;

    /// Repository used to load the submitted level.
    private final LevelRepository levelRepository;

    /// Service that provides the tileset used for replay serialization.
    private final TileSetService tileSetService;

    /// Service that loads the authenticated player.
    private final UserService userService;

    /// Mapper used to serialize replay payloads.
    private final ObjectMapper objectMapper;

    /// Service that upgrades legitimate replays to suspicious when fingerprints match.
    private final FingerprintSuspicionService suspicionService;

    /// Creates the replay submission service.
    public ReplaySubmissionService(final ReplayService replayService,
            final AttemptService attemptService,
            final LevelRepository levelRepository,
            final TileSetService tileSetService,
            final UserService userService,
            final ObjectMapper objectMapper,
            final FingerprintSuspicionService suspicionService) {
        this.replayService = replayService;
        this.attemptService = attemptService;
        this.levelRepository = levelRepository;
        this.tileSetService = tileSetService;
        this.userService = userService;
        this.objectMapper = objectMapper;
        this.suspicionService = suspicionService;
    }

    /// Replays a finished run and updates its anti-cheat status.
    public ReplayResultDTO submitRun(final String userId, final ReplayRequestDTO request) {
        final ch.usi.inf.bsc.sa4.lab02spring.model.Level level = levelRepository.findById(request.levelId())
                .orElseThrow(LevelNotFoundException::new);
        final ch.usi.inf.bsc.sa4.lab02spring.model.User user = userService.getById(userId)
                .orElseThrow(UserNotFoundException::new);

        level.ensurePlayable(userId);

        final String attemptId = request.attemptId();
        final ch.usi.inf.bsc.sa4.lab02spring.model.Attempt attempt = attemptService.getAttemptById(attemptId);
        if (!attempt.getUser().getId().equals(userId)) {
            throw new ForbiddenUserException("Attempt does not belong to this user");
        }

        final boolean playerCompleted = attempt.isCompleted();
        AntiCheatLog.levelCompleted(userId, request.levelId(), request.totalFrames());

        final ch.usi.inf.bsc.sa4.lab02spring.model.InputLogFingerprint fingerprint = InputLogFingerprintUtils
                .fingerprint(request.inputLog());
        attemptService.updateFingerprint(attemptId, user, request.levelId(), fingerprint);

        final ReplayResultDTO replayResult = replayService.replay(buildReplayRequest(userId, level, request));
        final AttemptVerificationStatus status = resolveStatus(level, user, attemptId, fingerprint, replayResult,
                playerCompleted, request.totalFrames());

        logVerificationStatus(userId, request, replayResult, playerCompleted, status);
        attemptService.updateAntiCheatStatus(attemptId, user, request.levelId(), status);
        return replayResult;
    }

    private ReplayRequest buildReplayRequest(final String userId,
            final ch.usi.inf.bsc.sa4.lab02spring.model.Level level,
            final ReplayRequestDTO request) {
        try {
            final ch.usi.inf.bsc.sa4.lab02spring.model.TileSet tileSet = tileSetService.getTileSet();
            final java.util.Map<String, Object> tiledMap = LayerToTiledMapConverter.convertPipeline(
                    level,
                    tileSet,
                    tileSetService);
            final String levelJson = objectMapper.writeValueAsString(tiledMap);
            final String inputJson = serializeInputLog(request);
            return new ReplayRequest(userId, request.levelId(), levelJson, inputJson);
        } catch (final JacksonException e) {
            AntiCheatLog.replayError(userId, request.levelId(), String.valueOf(e.getMessage()));
            throw new ReplaySerializationException(e);
        }
    }

    private AttemptVerificationStatus resolveStatus(
            final ch.usi.inf.bsc.sa4.lab02spring.model.Level level,
            final ch.usi.inf.bsc.sa4.lab02spring.model.User user,
            final String attemptId,
            final ch.usi.inf.bsc.sa4.lab02spring.model.InputLogFingerprint fingerprint,
            final ReplayResultDTO replayResult,
            final boolean playerCompleted,
            final int totalFrames) {
        AttemptVerificationStatus status = toAttemptVerificationStatus(replayResult, playerCompleted, totalFrames);
        if (status == AttemptVerificationStatus.LEGIT) {
            status = suspicionService.classify(level, user, attemptId, fingerprint);
        }
        return status;
    }

    private void logVerificationStatus(final String userId,
            final ReplayRequestDTO request,
            final ReplayResultDTO replayResult,
            final boolean playerCompleted,
            final AttemptVerificationStatus status) {
        switch (status) {
            case LEGIT -> AntiCheatLog.replayValid(userId, request.levelId(),
                    replayResult.reason() + " @ " + replayResult.frames() + " frames");
            case SUSPICIOUS ->
                AntiCheatLog.replaySuspicious(userId, request.levelId(), "InputLogFingerprint was found suspicious.");
            case CHEATED -> AntiCheatLog.replayMismatch(userId, request.levelId(),
                    mismatchedReason(replayResult, playerCompleted, request.totalFrames()));
            case REPLAY_ERROR -> AntiCheatLog.replayInvalid(userId, request.levelId(), replayResult.reason());
            default -> throw new IllegalStateException("Unexpected attempt verification status: " + status);
        }
    }

    private static String mismatchedReason(final ReplayResultDTO replayResult,
            final boolean playerCompleted,
            final int totalFrames) {
        String reason = "player did not complete level";
        if (playerCompleted) {
            if (isFrameCountMismatch(replayResult, totalFrames)) {
                reason = "frame count mismatch (timeScale tampering?): reported " + totalFrames
                        + " but replay took " + replayResult.frames();
            } else {
                reason = "player completed but replay ended in " + replayResult.reason();
            }
        }
        return reason;
    }

    private static AttemptVerificationStatus toAttemptVerificationStatus(final ReplayResultDTO replayResult,
            final boolean playerCompleted,
            final int totalFrames) {
        final AttemptVerificationStatus status;
        if (replayResult.valid()) {
            if (playerCompleted && "game_over".equals(replayResult.reason())) {
                status = AttemptVerificationStatus.CHEATED;
            } else if (isFrameCountMismatch(replayResult, totalFrames)) {
                status = AttemptVerificationStatus.CHEATED;
            } else {
                status = AttemptVerificationStatus.LEGIT;
            }
        } else {
            if (replayResult.reason().startsWith("error:")) {
                status = AttemptVerificationStatus.REPLAY_ERROR;
            } else {
                status = AttemptVerificationStatus.CHEATED;
            }
        }
        return status;
    }

    private static boolean isFrameCountMismatch(final ReplayResultDTO replayResult, final int totalFrames) {
        return "level_complete".equals(replayResult.reason())
                && Math.abs(replayResult.frames() - totalFrames) > FRAME_TOLERANCE;
    }

    private String serializeInputLog(final ReplayRequestDTO request) throws JacksonException {
        final List<SerializedReplayFrame> frames = request.inputLog().stream()
                .map(frame -> new SerializedReplayFrame(
                        frame.frame(),
                        new SerializedPlayerInput(frame.left(), frame.right(), frame.jump(), frame.run())))
                .toList();
        return objectMapper.writeValueAsString(frames);
    }

    private record SerializedReplayFrame(int frame, SerializedPlayerInput input) {
    }

    private record SerializedPlayerInput(boolean left, boolean right, boolean jump, boolean run) {
    }
}
