package ch.usi.inf.bsc.sa4.lab02spring.service.anticheat;

import ch.usi.inf.bsc.sa4.lab02spring.controller.ReplaySerializationException;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.InputFrameDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.ReplayRequestDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.ReplayResultDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.Attempt;
import ch.usi.inf.bsc.sa4.lab02spring.model.AttemptVerificationStatus;
import ch.usi.inf.bsc.sa4.lab02spring.model.InputLogFingerprint;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.repository.LevelRepository;
import ch.usi.inf.bsc.sa4.lab02spring.service.AttemptService;
import ch.usi.inf.bsc.sa4.lab02spring.service.TileCatalogService;
import ch.usi.inf.bsc.sa4.lab02spring.service.UserService;
import ch.usi.inf.bsc.sa4.lab02spring.utils.ForbiddenUserException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.LevelNotFoundException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.UserNotFoundException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.converter.LayerToTiledMapConverter;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

/// Tests for [ReplaySubmissionService].
@SpringBootTest
@DisplayName("The Replay Submission Service")
@SuppressWarnings("PMD.ExcessiveImports")
class ReplaySubmissionServiceTests {

    /// level complete string
    private static final String LEVEL_COMPLETE = "level_complete";

    /// level complete string
    private static final String GAME_OVER = "game_over";

    /// time stamp for level attempt
    private static final String TIMESTAMP = "2026-05-20T00:00:00Z";


    /// User id used by replay submission tests.
    private static final String USER_ID = "user-1";

    /// Level id used by replay submission tests.
    private static final String LEVEL_ID = "level-1";

    /// Attempt id used by replay submission tests.
    private static final String ATTEMPT_ID = "attempt-1";

    /// Reported frame count used by replay submission tests.
    private static final int TOTAL_FRAMES = 42;

    /// Serialized level payload passed to the replay service.
    private static final String LEVEL_JSON = "{\"layers\":[]}";

    /// Serialized input payload passed to the replay service.
    private static final String INPUT_JSON = "[]";

    /// Replay request DTO used by submission tests.
    private static final ReplayRequestDTO REQUEST = new ReplayRequestDTO(
            LEVEL_ID,
            ATTEMPT_ID,
            TOTAL_FRAMES,
            List.of(
                    new InputFrameDTO(0, false, false, false, false),
                    new InputFrameDTO(1, true, false, false, false)));

    /// Minimal tiled map returned by the mocked converter.
    private static final Map<String, Object> TILED_MAP = Map.of("layers", List.of());

    /// Service under test.
    @Autowired
    private ReplaySubmissionService service;

    /// Mocked replay process service.
    @MockitoBean
    private ReplayService replayService;

    /// Mocked attempt service.
    @MockitoBean
    private AttemptService attemptService;

    /// Mocked level repository.
    @MockitoBean
    private LevelRepository levelRepository;

    /// Mocked tileset service.
    @MockitoBean
    private TileCatalogService tileCatalogService;

    /// Mocked user service.
    @MockitoBean
    private UserService userService;

    /// Mocked JSON mapper.
    @MockitoBean
    private JsonMapper objectMapper;

    /// Mocked fingerprint suspicion service.
    @MockitoBean
    private FingerprintSuspicionService suspicionService;

    /// Test user fixture.
    private User user;

    /// Test level fixture.
    private Level level;

    /// Completed attempt fixture.
    private Attempt completedAttempt;

    /// Creates fresh test fixtures.
    @BeforeEach
    void setUp() {
        user = new User(USER_ID, "Mario");
        level = new Level("Replay", "desc", user);
        completedAttempt = new Attempt(
                user,
                ZonedDateTime.parse(TIMESTAMP),
                level,
                true,
                Duration.ofSeconds(10));
    }

    /// Tests for validation before replay execution.
    @Nested
    @DisplayName("when validating a replay submission")
    class Validation {

        /// Checks that missing levels are rejected before other work.
        @Test
        @DisplayName("throws LevelNotFoundException when the level does not exist")
        void throwsWhenLevelDoesNotExist() {
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.empty());

            Assertions.assertThrows(LevelNotFoundException.class,
                    () -> service.submitRun(USER_ID, REQUEST));

            Mockito.verifyNoInteractions(userService, attemptService, replayService);
        }

        /// Checks that missing users are rejected.
        @Test
        @DisplayName("throws UserNotFoundException when the user does not exist")
        void throwsWhenUserDoesNotExist() {
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));
            Mockito.when(userService.getById(USER_ID)).thenReturn(Optional.empty());

            Assertions.assertThrows(UserNotFoundException.class,
                    () -> service.submitRun(USER_ID, REQUEST));

            Mockito.verifyNoInteractions(attemptService, replayService);
        }

        /// Checks that a user cannot submit someone else's attempt.
        @Test
        @DisplayName("throws ForbiddenUserException when the attempt belongs to another user")
        void throwsWhenAttemptBelongsToAnotherUser() {
            final User otherUser = new User("other-user", "Luigi");
            final Attempt otherAttempt = new Attempt(
                    otherUser,
                    ZonedDateTime.parse(TIMESTAMP),
                    level,
                    true,
                    Duration.ofSeconds(10));
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));
            Mockito.when(userService.getById(USER_ID)).thenReturn(Optional.of(user));
            Mockito.when(attemptService.getAttemptById(ATTEMPT_ID)).thenReturn(otherAttempt);

            Assertions.assertThrows(ForbiddenUserException.class,
                    () -> service.submitRun(USER_ID, REQUEST));

            Mockito.verify(replayService, Mockito.never()).replay(Mockito.any());
        }
    }

    /// Tests for persisted anti-cheat status decisions.
    @Nested
    @DisplayName("when resolving anti-cheat status")
    class StatusResolution {

        /// Checks the successful replay and fingerprint path.
        @Test
        @DisplayName("stores LEGIT when replay passes and fingerprint is clean")
        void storesLegitWhenReplayPassesAndFingerprintIsClean() throws JacksonException {
            final ReplayResultDTO replayResult = new ReplayResultDTO(true, LEVEL_COMPLETE, TOTAL_FRAMES);
            givenReplayDependencies(completedAttempt, replayResult);
            Mockito.when(suspicionService.classify(
                    Mockito.eq(level),
                    Mockito.eq(user),
                    Mockito.eq(ATTEMPT_ID),
                    Mockito.any(InputLogFingerprint.class)))
                    .thenReturn(AttemptVerificationStatus.LEGIT);

            try (MockedStatic<LayerToTiledMapConverter> ignored = mockTiledMapConversion()) {
                final ReplayResultDTO result = service.submitRun(USER_ID, REQUEST);

                Assertions.assertEquals(
                        new ReplayResultDTO(true, LEVEL_COMPLETE, TOTAL_FRAMES, AttemptVerificationStatus.LEGIT),
                        result);
                verifyReplayRequest();
                verifyFingerprintUpdatedAndClassified();
                Mockito.verify(attemptService).updateAntiCheatStatus(
                        ATTEMPT_ID,
                        user,
                        LEVEL_ID,
                        AttemptVerificationStatus.LEGIT);
            }
        }

        /// Checks that a clean replay can still become suspicious by fingerprint.
        @Test
        @DisplayName("stores SUSPICIOUS when fingerprint classification is suspicious")
        void storesSuspiciousWhenFingerprintClassificationIsSuspicious() throws JacksonException {
            final ReplayResultDTO replayResult = new ReplayResultDTO(true, LEVEL_COMPLETE, TOTAL_FRAMES);
            givenReplayDependencies(completedAttempt, replayResult);
            Mockito.when(suspicionService.classify(
                    Mockito.eq(level),
                    Mockito.eq(user),
                    Mockito.eq(ATTEMPT_ID),
                    Mockito.any(InputLogFingerprint.class)))
                    .thenReturn(AttemptVerificationStatus.SUSPICIOUS);

            try (MockedStatic<LayerToTiledMapConverter> ignored = mockTiledMapConversion()) {
                service.submitRun(USER_ID, REQUEST);

                Mockito.verify(attemptService).updateAntiCheatStatus(
                        ATTEMPT_ID,
                        user,
                        LEVEL_ID,
                        AttemptVerificationStatus.SUSPICIOUS);
            }
        }

        /// Checks frame-count mismatch detection.
        @Test
        @DisplayName("stores CHEATED when replay frame count differs beyond tolerance")
        void storesCheatedWhenReplayFrameCountDiffersBeyondTolerance() throws JacksonException {
            final ReplayResultDTO replayResult = new ReplayResultDTO(true, LEVEL_COMPLETE, TOTAL_FRAMES + 6);
            givenReplayDependencies(completedAttempt, replayResult);

            try (MockedStatic<LayerToTiledMapConverter> ignored = mockTiledMapConversion()) {
                service.submitRun(USER_ID, REQUEST);

                Mockito.verify(attemptService).updateAntiCheatStatus(
                        ATTEMPT_ID,
                        user,
                        LEVEL_ID,
                        AttemptVerificationStatus.CHEATED);
                Mockito.verifyNoInteractions(suspicionService);
            }
        }

        /// Checks completed attempts that replay into game-over.
        @Test
        @DisplayName("stores CHEATED when a completed attempt replays as game over")
        void storesCheatedWhenCompletedAttemptReplaysAsGameOver() throws JacksonException {
            final ReplayResultDTO replayResult = new ReplayResultDTO(true, GAME_OVER, TOTAL_FRAMES);
            givenReplayDependencies(completedAttempt, replayResult);

            try (MockedStatic<LayerToTiledMapConverter> ignored = mockTiledMapConversion()) {
                service.submitRun(USER_ID, REQUEST);

                Mockito.verify(attemptService).updateAntiCheatStatus(
                        ATTEMPT_ID,
                        user,
                        LEVEL_ID,
                        AttemptVerificationStatus.CHEATED);
                Mockito.verifyNoInteractions(suspicionService);
            }
        }

        /// Checks incomplete player attempts that replay as game-over cleanly.
        @Test
        @DisplayName("stores LEGIT when an incomplete attempt replays as game over and fingerprint is clean")
        void storesLegitWhenIncompleteAttemptReplaysAsGameOverAndFingerprintIsClean() throws JacksonException {
            final Attempt incompleteAttempt = new Attempt(
                    user,
                    ZonedDateTime.parse("2026-05-20T00:00:00Z"),
                    level,
                    false,
                    Duration.ofSeconds(10));
            final ReplayResultDTO replayResult = new ReplayResultDTO(true, GAME_OVER,TOTAL_FRAMES);
            givenReplayDependencies(incompleteAttempt, replayResult);
            Mockito.when(suspicionService.classify(
                    Mockito.eq(level),
                    Mockito.eq(user),
                    Mockito.eq(ATTEMPT_ID),
                    Mockito.any(InputLogFingerprint.class)))
                    .thenReturn(AttemptVerificationStatus.LEGIT);

            try (MockedStatic<LayerToTiledMapConverter> ignored = mockTiledMapConversion()) {
                service.submitRun(USER_ID, REQUEST);

                Mockito.verify(attemptService).updateAntiCheatStatus(
                        ATTEMPT_ID,
                        user,
                        LEVEL_ID,
                        AttemptVerificationStatus.LEGIT);
            }
        }

        /// Checks non-error replay failures.
        @Test
        @DisplayName("stores CHEATED when replay fails without an error reason")
        void storesCheatedWhenReplayFailsWithoutErrorReason() throws JacksonException {
            final ReplayResultDTO replayResult = new ReplayResultDTO(false, GAME_OVER, TOTAL_FRAMES);
            givenReplayDependencies(completedAttempt, replayResult);

            try (MockedStatic<LayerToTiledMapConverter> ignored = mockTiledMapConversion()) {
                service.submitRun(USER_ID, REQUEST);

                Mockito.verify(attemptService).updateAntiCheatStatus(
                        ATTEMPT_ID,
                        user,
                        LEVEL_ID,
                        AttemptVerificationStatus.CHEATED);
                Mockito.verifyNoInteractions(suspicionService);
            }
        }

        /// Checks failed player attempts that also fail replay.
        @Test
        @DisplayName("stores CHEATED when an incomplete player attempt fails replay")
        void storesCheatedWhenIncompletePlayerAttemptFailsReplay() throws JacksonException {
            final Attempt incompleteAttempt = new Attempt(
                    user,
                    ZonedDateTime.parse("2026-05-20T00:00:00Z"),
                    level,
                    false,
                    Duration.ofSeconds(10));
            final ReplayResultDTO replayResult = new ReplayResultDTO(false, GAME_OVER, TOTAL_FRAMES);
            givenReplayDependencies(incompleteAttempt, replayResult);

            try (MockedStatic<LayerToTiledMapConverter> ignored = mockTiledMapConversion()) {
                service.submitRun(USER_ID, REQUEST);

                Mockito.verify(attemptService).updateAntiCheatStatus(
                        ATTEMPT_ID,
                        user,
                        LEVEL_ID,
                        AttemptVerificationStatus.CHEATED);
                Mockito.verifyNoInteractions(suspicionService);
            }
        }

        /// Checks replay process errors.
        @Test
        @DisplayName("stores REPLAY_ERROR when replay returns an error reason")
        void storesReplayErrorWhenReplayReturnsErrorReason() throws JacksonException {
            final ReplayResultDTO replayResult = new ReplayResultDTO(false, "error:npx_not_found", 0);
            givenReplayDependencies(completedAttempt, replayResult);

            try (MockedStatic<LayerToTiledMapConverter> ignored = mockTiledMapConversion()) {
                service.submitRun(USER_ID, REQUEST);

                Mockito.verify(attemptService).updateAntiCheatStatus(
                        ATTEMPT_ID,
                        user,
                        LEVEL_ID,
                        AttemptVerificationStatus.REPLAY_ERROR);
                Mockito.verifyNoInteractions(suspicionService);
            }
        }

        /// Checks serialization failures before replay execution.
        @Test
        @DisplayName("throws ReplaySerializationException when replay payload serialization fails")
        void throwsWhenReplayPayloadSerializationFails() throws JacksonException {
            givenReplayDependencies(completedAttempt, new ReplayResultDTO(true, "level_complete", TOTAL_FRAMES));
            Mockito.when(objectMapper.writeValueAsString(Mockito.any()))
                    .thenThrow(new JacksonException("boom") {
                        private static final long serialVersionUID = 1L;
                    });

            try (MockedStatic<LayerToTiledMapConverter> ignored = mockTiledMapConversion()) {
                Assertions.assertThrows(ReplaySerializationException.class,
                        () -> service.submitRun(USER_ID, REQUEST));

                Mockito.verify(replayService, Mockito.never()).replay(Mockito.any());
                Mockito.verify(attemptService, Mockito.never()).updateAntiCheatStatus(
                        Mockito.anyString(),
                        Mockito.any(User.class),
                        Mockito.anyString(),
                        Mockito.any(AttemptVerificationStatus.class));
            }
        }

        /// Checks unexpected classifier output fails explicitly.
        @Test
        @DisplayName("throws IllegalStateException when classifier returns NOT_VERIFIED")
        void throwsWhenClassifierReturnsNotVerified() throws JacksonException {
            final ReplayResultDTO replayResult = new ReplayResultDTO(true, "level_complete", TOTAL_FRAMES);
            givenReplayDependencies(completedAttempt, replayResult);
            Mockito.when(suspicionService.classify(
                    Mockito.eq(level),
                    Mockito.eq(user),
                    Mockito.eq(ATTEMPT_ID),
                    Mockito.any(InputLogFingerprint.class)))
                    .thenReturn(AttemptVerificationStatus.NOT_VERIFIED);

            try (MockedStatic<LayerToTiledMapConverter> ignored = mockTiledMapConversion()) {
                Assertions.assertThrows(IllegalStateException.class,
                        () -> service.submitRun(USER_ID, REQUEST));
            }
        }
    }

    /// Stubs the direct collaborators needed for a replay submission.
    private void givenReplayDependencies(final Attempt attempt,
            final ReplayResultDTO replayResult) throws JacksonException {
        Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));
        Mockito.when(userService.getById(USER_ID)).thenReturn(Optional.of(user));
        Mockito.when(attemptService.getAttemptById(ATTEMPT_ID)).thenReturn(attempt);
        Mockito.when(objectMapper.writeValueAsString(Mockito.any()))
                .thenReturn(LEVEL_JSON, INPUT_JSON);
        Mockito.when(replayService.replay(Mockito.any(ReplayRequest.class))).thenReturn(replayResult);
    }

    /// Mocks the static Tiled map conversion used by the service.
    private MockedStatic<LayerToTiledMapConverter> mockTiledMapConversion() {
        final MockedStatic<LayerToTiledMapConverter> mockedStatic = Mockito.mockStatic(LayerToTiledMapConverter.class);
        mockedStatic.when(() -> LayerToTiledMapConverter.convertPipeline(
                level,
                tileCatalogService)).thenReturn(TILED_MAP);
        return mockedStatic;
    }

    /// Verifies the replay service receives the serialized replay payload.
    private void verifyReplayRequest() {
        final ArgumentCaptor<ReplayRequest> replayRequestCaptor = ArgumentCaptor.forClass(ReplayRequest.class);
        Mockito.verify(replayService).replay(replayRequestCaptor.capture());
        Assertions.assertEquals(
                new ReplayRequest(USER_ID, LEVEL_ID, LEVEL_JSON, INPUT_JSON),
                replayRequestCaptor.getValue());
    }

    /// Verifies the same computed fingerprint is persisted and classified.
    private void verifyFingerprintUpdatedAndClassified() {
        final ArgumentCaptor<InputLogFingerprint> fingerprintCaptor = ArgumentCaptor
                .forClass(InputLogFingerprint.class);
        Mockito.verify(attemptService).updateFingerprint(
                Mockito.eq(ATTEMPT_ID),
                Mockito.eq(user),
                Mockito.eq(LEVEL_ID),
                fingerprintCaptor.capture());
        Mockito.verify(suspicionService).classify(
                level,
                user,
                ATTEMPT_ID,
                fingerprintCaptor.getValue());
    }
}
