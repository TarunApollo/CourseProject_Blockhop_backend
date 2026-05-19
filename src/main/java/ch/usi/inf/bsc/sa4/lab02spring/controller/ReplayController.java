package ch.usi.inf.bsc.sa4.lab02spring.controller;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.ReplayRequestDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.ReplayResultDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.Attempt;
import ch.usi.inf.bsc.sa4.lab02spring.model.AttemptVerificationStatus;
import ch.usi.inf.bsc.sa4.lab02spring.model.InputLogFingerprint;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.TileSet;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.repository.LevelRepository;
import ch.usi.inf.bsc.sa4.lab02spring.service.AttemptService;
import ch.usi.inf.bsc.sa4.lab02spring.service.TileSetService;
import ch.usi.inf.bsc.sa4.lab02spring.service.UserService;
import ch.usi.inf.bsc.sa4.lab02spring.service.anticheat.AntiCheatLog;
import ch.usi.inf.bsc.sa4.lab02spring.service.anticheat.FingerprintSuspicionService;
import ch.usi.inf.bsc.sa4.lab02spring.service.anticheat.ReplayRequest;
import ch.usi.inf.bsc.sa4.lab02spring.service.anticheat.ReplayService;
import ch.usi.inf.bsc.sa4.lab02spring.utils.ForbiddenUserException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.LevelNotFoundException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.OAuth2UserUtils;
import ch.usi.inf.bsc.sa4.lab02spring.utils.UserNotFoundException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.anticheat.InputLogFingerprintUtils;
import ch.usi.inf.bsc.sa4.lab02spring.utils.converter.LayerToTiledMapConverter;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

/// Controller exposing replay-based anti-cheat endpoints for level attempts.
@RestController
@RequestMapping("/replay")
public class ReplayController {

    /// OAuth2 subject attribute name.
    private static final String OAUTH_SUB_ATTRIBUTE = "sub";

    /// Service that runs the frontend replay simulation.
    private final ReplayService replayService;

    /// Service that owns attempt lookup and anti-cheat persistence.
    private final AttemptService attemptService;

    /// Repository used to load levels submitted for replay verification.
    private final LevelRepository levelRepository;

    /// Service providing the tileset needed to serialize levels for replay.
    private final TileSetService tileSetService;

    /// Service used to load the authenticated player account.
    private final UserService userService;

    /// Mapper used to serialize replay inputs and level maps.
    private final ObjectMapper objectMapper;

    /// Service that classifies fingerprint suspicion based on duplicate detection.
    private final FingerprintSuspicionService fingerprintSuspicionService;

    /// Constructs the replay controller with its replay and persistence
    /// dependencies.
    ///
    /// @param replayService                  service that executes replay simulations
    /// @param attemptService                 service that reads and updates attempts
    /// @param levelRepository                repository for loading levels
    /// @param tileSetService                 service that provides tileset data
    /// @param userService                    service that loads users
    /// @param objectMapper                   serializer used for replay payloads
    /// @param fingerprintSuspicionService    service that classifies fingerprint suspicion
    public ReplayController(final ReplayService replayService,
            final AttemptService attemptService,
            final LevelRepository levelRepository,
            final TileSetService tileSetService,
            final UserService userService,
            final ObjectMapper objectMapper,
            final FingerprintSuspicionService fingerprintSuspicionService) {
        this.replayService = replayService;
        this.attemptService = attemptService;
        this.levelRepository = levelRepository;
        this.tileSetService = tileSetService;
        this.userService = userService;
        this.objectMapper = objectMapper;
        this.fingerprintSuspicionService = fingerprintSuspicionService;
    }

    /// Validates that a player can start a replay-tracked run on a level.
    ///
    /// @param oauth2User authenticated OAuth2 user principal
    /// @param request    start request containing the level id
    /// @return an empty success response when the level can be played
    @PostMapping("/start")
    public ResponseEntity<Void> startRun(
            @AuthenticationPrincipal final OAuth2User oauth2User,
            @RequestBody final StartRequest request) {

        final String userId = OAuth2UserUtils.getRequiredAttribute(oauth2User, OAUTH_SUB_ATTRIBUTE);

        final Level level = levelRepository.findById(request.levelId())
                .orElseThrow(LevelNotFoundException::new);

        level.ensurePlayable(userId);

        AntiCheatLog.levelStarted(userId, request.levelId());
        return ResponseEntity.ok().build();
    }

    /// Submits a completed run for replay execution and anti-cheat classification.
    ///
    /// @param oauth2User authenticated OAuth2 user principal
    /// @param request    replay request containing attempt, level, frame, and input
    ///                   data
    /// @return the replay result produced by the anti-cheat simulation
    @PostMapping("/submit")
    public ResponseEntity<ReplayResultDTO> submitRun(
            @AuthenticationPrincipal final OAuth2User oauth2User,
            @RequestBody final ReplayRequestDTO request) {

        final String userId = OAuth2UserUtils.getRequiredAttribute(oauth2User, OAUTH_SUB_ATTRIBUTE);

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

        final InputLogFingerprint fingerprint = InputLogFingerprintUtils.fingerprint(request.inputLog());
        attemptService.updateFingerprint(attemptId, user, request.levelId(), fingerprint);

        final TileSet tileSet = tileSetService.getTileSet();
        final Map<String, Object> tiledMap = LayerToTiledMapConverter.convertPipeline(
                level, tileSet, tileSetService);

        final String levelJson;
        final String inputJson;
        try {
            levelJson = objectMapper.writeValueAsString(tiledMap);
            inputJson = serializeInputLog(request);
        } catch (final JacksonException e) {
            AntiCheatLog.replayError(userId, request.levelId(), String.valueOf(e.getMessage()));
            throw new ReplaySerializationException(e);
        }

        final ReplayRequest replayRequest = new ReplayRequest(
                userId,
                request.levelId(),
                levelJson,
                inputJson);
        final ReplayResultDTO result = replayService.replay(replayRequest);

        AttemptVerificationStatus status = toAttemptVerificationStatus(result, playerCompleted, request.totalFrames());

        if (status == AttemptVerificationStatus.LEGIT) {
            status = fingerprintSuspicionService.classify(level, user, attemptId, fingerprint);
        }

        switch (status) {
            case LEGIT ->
                AntiCheatLog.replayValid(userId, request.levelId(),
                        result.reason() + " @ " + result.frames() + " frames");
            case SUSPICIOUS ->
                AntiCheatLog.replaySuspicious(userId, request.levelId(), "InputLogFingerprint was found suspicious.");
            case CHEATED ->
                AntiCheatLog.replayMismatch(userId, request.levelId(),
                        mismatchedReason(result, playerCompleted, request.totalFrames()));
            case REPLAY_ERROR ->
                AntiCheatLog.replayInvalid(userId, request.levelId(), result.reason());
        }

        attemptService.updateAntiCheatStatus(
                attemptId,
                user,
                request.levelId(),
                status);

        return ResponseEntity.ok(result);
    }

    /// Builds the reason stored when the browser-reported attempt and replay result
    /// disagree.
    ///
    /// @param result          replay simulation result
    /// @param playerCompleted whether the original attempt completed the level
    /// @param totalFrames     browser-reported input frame count
    /// @return a human-readable mismatch reason
    private static String mismatchedReason(final ReplayResultDTO result,
            final boolean playerCompleted,
            final int totalFrames) {
        final String reason;
        if (!playerCompleted) {
            reason = "player did not complete level";
        } else if ("level_complete".equals(result.reason()) && Math.abs(result.frames() - totalFrames) > 5) {
            reason = "frame count mismatch (timeScale tampering?): reported " + totalFrames
                    + " but replay took " + result.frames();
        } else {
            reason = "player completed but replay ended in " + result.reason();
        }
        return reason;
    }

    /// Maps a replay result to an attempt verification status.
    ///
    /// The framecount comparison between a valid `level_complete` replay and the
    /// reported total frame count catches framerate hack cheats such as `setFps` or
    /// `timeScale` changes. With a reduced browser time scale, each physics step
    /// moves the player less, requiring more input log entries to reach the door;
    /// the headless replay processes entries at time scale 1.0 and therefore
    /// finishes earlier.
    ///
    /// @param result          replay simulation result
    /// @param playerCompleted whether the original attempt completed the level
    /// @param totalFrames     browser-reported input frame count
    /// @return the anti-cheat verification status for the attempt
    private static AttemptVerificationStatus toAttemptVerificationStatus(final ReplayResultDTO result,
            final boolean playerCompleted,
            final int totalFrames) {
        final AttemptVerificationStatus status;
        if (!result.valid()) {
            status = result.reason().startsWith("error:")
                    ? AttemptVerificationStatus.REPLAY_ERROR
                    : AttemptVerificationStatus.CHEATED;
        } else if (playerCompleted && "game_over".equals(result.reason())) {
            status = AttemptVerificationStatus.CHEATED;
        } else if ("level_complete".equals(result.reason()) && Math.abs(result.frames() - totalFrames) > 5) {
            // +- frames should absorb clock jitter
            status = AttemptVerificationStatus.CHEATED;
        } else {
            status = AttemptVerificationStatus.LEGIT;
        }
        return status;
    }

    /// Converts submitted input frames into the JSON shape consumed by the replay
    /// script.
    ///
    /// @param request submitted replay request
    /// @return serialized replay frame list
    private String serializeInputLog(final ReplayRequestDTO request) throws JacksonException {
        final List<SerializedReplayFrame> frames = request.inputLog().stream()
                .map(frame -> new SerializedReplayFrame(
                        frame.frame(),
                        new SerializedPlayerInput(frame.left(), frame.right(), frame.jump(), frame.run())))
                .toList();
        return objectMapper.writeValueAsString(frames);
    }

    /// Request body used when a player starts a replay-tracked level run.
    ///
    /// @param levelId id of the level being started
    public record StartRequest(String levelId) {
    }

    /// Serialized frame entry passed to the frontend replay script.
    ///
    /// @param frame frame number in the input log
    /// @param input player input state for the frame
    private record SerializedReplayFrame(int frame, SerializedPlayerInput input) {
    }

    /// Serialized player input state passed to the frontend replay script.
    ///
    /// @param left  whether the left input is active
    /// @param right whether the right input is active
    /// @param jump  whether the jump input is active
    /// @param run   whether the run input is active
    private record SerializedPlayerInput(boolean left, boolean right, boolean jump, boolean run) {
    }
}
