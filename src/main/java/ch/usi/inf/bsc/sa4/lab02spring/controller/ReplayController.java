package ch.usi.inf.bsc.sa4.lab02spring.controller;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.ReplayRequestDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.ReplayResultDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.Attempt;
import ch.usi.inf.bsc.sa4.lab02spring.model.AttemptVerificationStatus;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.TileSet;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.repository.LevelRepository;
import ch.usi.inf.bsc.sa4.lab02spring.service.AttemptService;
import ch.usi.inf.bsc.sa4.lab02spring.service.TileSetService;
import ch.usi.inf.bsc.sa4.lab02spring.service.antiCheat.AntiCheatLog;
import ch.usi.inf.bsc.sa4.lab02spring.service.antiCheat.ReplayService;
import ch.usi.inf.bsc.sa4.lab02spring.service.UserService;
import ch.usi.inf.bsc.sa4.lab02spring.utils.AuthUtils;
import ch.usi.inf.bsc.sa4.lab02spring.utils.ForbiddenUserException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.LevelNotFoundException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.UserNotFoundException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.converter.LayerToTiledMapConverter;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/replay")
public class ReplayController {

    private final ReplayService replayService;
    private final AttemptService attemptService;
    private final LevelRepository levelRepository;
    private final TileSetService tileSetService;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    public ReplayController(final ReplayService replayService,
                            final AttemptService attemptService,
                            final LevelRepository levelRepository,
                            final TileSetService tileSetService,
                            final UserService userService,
                            final ObjectMapper objectMapper) {
        this.replayService = replayService;
        this.attemptService = attemptService;
        this.levelRepository = levelRepository;
        this.tileSetService = tileSetService;
        this.userService = userService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/start")
    public ResponseEntity<Void> startRun(
            final Authentication authentication,
            @RequestBody final StartRequest request) {

        final @Nullable String userId = AuthUtils.getUserIdFromAuth(authentication);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        final Level level = levelRepository.findById(request.levelId())
                .orElseThrow(LevelNotFoundException::new);

        level.ensurePlayable(userId);

        AntiCheatLog.levelStarted(userId, request.levelId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/submit")
    public ResponseEntity<ReplayResultDTO> submitRun(
            final Authentication authentication,
            @RequestBody final ReplayRequestDTO request) {

        final @Nullable String userId = AuthUtils.getUserIdFromAuth(authentication);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        final Level level = levelRepository.findById(request.levelId())
                .orElseThrow(LevelNotFoundException::new);
        final User user = userService.getById(userId).orElseThrow(UserNotFoundException::new);

        level.ensurePlayable(userId);

        // Validate attempt ownership before doing any expensive work (replay).
        // This prevents a user from submitting someone else's attemptId and
        // overwriting their anticheat status after the replay runs.
        final String attemptId = request.attemptId();
        final Attempt attempt = attemptService.getAttemptById(attemptId);
        if (!attempt.getUser().getId().equals(userId)) {
            throw new ForbiddenUserException("Attempt does not belong to this user");
        }
        final boolean playerCompleted = attempt.isCompleted();

        AntiCheatLog.levelCompleted(userId, request.levelId(), request.totalFrames());

        final TileSet tileSet = tileSetService.getTileSet();
        final Map<String, Object> tiledMap = LayerToTiledMapConverter.convertPipeline(
                level, tileSet, tileSetService);

        final String levelJson;
        final String inputJson;
        try {
            levelJson = objectMapper.writeValueAsString(tiledMap);
            inputJson = serializeInputLog(request);
        } catch (final Exception e) {
            AntiCheatLog.replayError(userId, request.levelId(), String.valueOf(e.getMessage()));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        final ReplayResultDTO result = replayService.replay(
                userId, request.levelId(), levelJson, inputJson);

        final AttemptVerificationStatus status = toAttemptVerificationStatus(result, playerCompleted, request.totalFrames());

        switch (status) {
            case LEGIT ->
                AntiCheatLog.replayValid(userId, request.levelId(), result.reason() + " @ " + result.frames() + " frames");
            case CHEATED ->
                AntiCheatLog.replayMismatch(userId, request.levelId(), mismatchedReason(result, playerCompleted, request.totalFrames()));
            case REPLAY_ERROR ->
                AntiCheatLog.replayInvalid(userId, request.levelId(), result.reason());
            default -> {}
        }

        attemptService.updateAntiCheatStatus(
                attemptId,
                user,
                request.levelId(),
                status);

        return ResponseEntity.ok(result);
    }

    private static String mismatchedReason(final ReplayResultDTO result,
                                            final boolean playerCompleted,
                                            final int totalFrames) {
        if (!playerCompleted) {
            return "player did not complete level";
        }
        if ("level_complete".equals(result.reason()) && Math.abs(result.frames() - totalFrames) > 5) {
            return "frame count mismatch (timeScale tampering?): reported " + totalFrames
                    + " but replay took " + result.frames();
        }
        return "player completed but replay ended in " + result.reason();
    }


    // Maps a replay result to an attempt verification status.
    //
    // The framecount comparison (valid + level_complete vs.
    // reported totalFrames) catches framerate hack cheats
    // (setFps / timeScale changes). When the browser runs at a
    // reduced timeScale each physics step moves the player less, so
    // the browser needs more steps and therefore more input log
    // entries to reach the door. The headless replay processes all
    // entries at timeScale 1.0, reaches the door earlier, and
    // reports a lower frame count. A mismatch flags the tampering.

    private static AttemptVerificationStatus toAttemptVerificationStatus(final ReplayResultDTO result,
                                                                          final boolean playerCompleted,
                                                                          final int totalFrames) {
        if (!result.valid()) {
            return result.reason().startsWith("error:")
                    ? AttemptVerificationStatus.REPLAY_ERROR
                    : AttemptVerificationStatus.CHEATED;
        }
        if ("game_over".equals(result.reason()) && playerCompleted) {
            return AttemptVerificationStatus.CHEATED;
        }
        /// +- frames should absorb clock jitter
        if ("level_complete".equals(result.reason()) && Math.abs(result.frames() - totalFrames) > 5) {
            return AttemptVerificationStatus.CHEATED;
        }
        return AttemptVerificationStatus.LEGIT;
    }

    private String serializeInputLog(final ReplayRequestDTO request) throws Exception {
        final List<SerializedReplayFrame> frames = request.inputLog().stream()
                .map(frame -> new SerializedReplayFrame(
                        frame.frame(),
                        new SerializedPlayerInput(frame.left(), frame.right(), frame.jump(), frame.run())))
                .toList();
        return objectMapper.writeValueAsString(frames);
    }

    public record StartRequest(String levelId) {
    }

    private record SerializedReplayFrame(int frame, SerializedPlayerInput input) {
    }

    private record SerializedPlayerInput(boolean left, boolean right, boolean jump, boolean run) {
    }
}
