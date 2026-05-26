package ch.usi.inf.bsc.sa4.lab02spring.service.level;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.GhostDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.InputFrameDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.Attempt;
import ch.usi.inf.bsc.sa4.lab02spring.model.AttemptVerificationStatus;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.repository.AttemptRepository;
import ch.usi.inf.bsc.sa4.lab02spring.repository.LevelRepository;
import ch.usi.inf.bsc.sa4.lab02spring.utils.LevelNotFoundException;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;

/// Unit tests for [GhostService].
@SpringBootTest
@DisplayName("The Ghost Service")
class GhostServiceTests {

    /// Valid MongoDB id used for level lookup and ghost queries.
    private static final String LEVEL_ID = "64b64c9f6f4b2c0012345678";

    /// Valid MongoDB id used for the replayable attempt fixture.
    private static final String ATTEMPT_ID = "64b64c9f6f4b2c0012345679";

    /// Fixed timestamp used for attempt fixtures.
    private static final ZonedDateTime TIMESTAMP = ZonedDateTime.parse("2026-05-20T00:00:00Z");

    /// Service under test.
    @Autowired
    private GhostService ghostService;

    /// Mocked level repository.
    @MockitoBean
    private LevelRepository levelRepository;

    /// Mocked attempt repository.
    @MockitoBean
    private AttemptRepository attemptRepository;

    /// User requesting the ghost.
    private User currentUser;

    /// User that owns the fastest ghost attempt.
    private User holderUser;

    /// Published level used by successful lookups.
    private Level publishedLevel;

    /// Replay input log returned by the ghost attempt.
    private List<InputFrameDTO> inputLog;

    /// Replayable attempt returned by the ghost query.
    private Attempt ghostAttempt;

    /// Sets up reusable fixtures before each test.
    @BeforeEach
    void setup() {
        this.currentUser = new User("user-1", "Mario");
        this.holderUser = new User("user-2", "Luigi");
        this.publishedLevel = new Level("Level", "desc", this.holderUser);
        ReflectionTestUtils.setField(this.publishedLevel, "id", LEVEL_ID);
        ReflectionTestUtils.setField(this.publishedLevel, "published", true);

        this.inputLog = List.of(
                new InputFrameDTO(0, false, true, false, true),
                new InputFrameDTO(1, false, true, true, true));
        this.ghostAttempt = new Attempt(
                ATTEMPT_ID,
                this.holderUser,
                TIMESTAMP,
                this.publishedLevel,
                true,
                Duration.ofSeconds(12),
                AttemptVerificationStatus.NOT_VERIFIED);
        this.ghostAttempt.setInputLog(this.inputLog);
    }

    /// Tests related to retrieving a ghost replay.
    @Nested
    @DisplayName("when retrieving a ghost")
    class Retrieval {

        /// Verifies that an unknown level is surfaced as not found.
        @Test
        @DisplayName("throws LevelNotFoundException when the level does not exist")
        void throwsWhenLevelDoesNotExist() {
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.empty());

            Assertions.assertThrows(LevelNotFoundException.class,
                    () -> ghostService.getGhostForLevel(LEVEL_ID, currentUser));
            Mockito.verifyNoInteractions(attemptRepository);
        }

        /// Verifies that unpublished levels are hidden behind the same not-found error.
        @Test
        @DisplayName("throws LevelNotFoundException when the level is unpublished")
        void throwsWhenLevelIsUnpublished() {
            ReflectionTestUtils.setField(publishedLevel, "published", false);
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(publishedLevel));

            Assertions.assertThrows(LevelNotFoundException.class,
                    () -> ghostService.getGhostForLevel(LEVEL_ID, currentUser));
            Mockito.verifyNoInteractions(attemptRepository);
        }

        /// Verifies that a user must complete the level before seeing a ghost.
        @Test
        @DisplayName("returns empty when the current user has not completed the level")
        void returnsEmptyWhenCurrentUserHasNotCompleted() {
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(publishedLevel));
            Mockito.when(attemptRepository.existsByUserAndLevelAndCompletedTrue(currentUser, publishedLevel))
                    .thenReturn(false);

            final Optional<GhostDTO> result = ghostService.getGhostForLevel(LEVEL_ID, currentUser);

            Assertions.assertTrue(result.isEmpty());
            Mockito.verify(attemptRepository, Mockito.never()).findFastestGhostCandidate(Mockito.any());
        }

        /// Verifies that a completed user still gets no ghost when no replay is available.
        @Test
        @DisplayName("returns empty when no replayable completed attempt exists")
        void returnsEmptyWhenNoGhostCandidateExists() {
            final ObjectId levelObjectId = new ObjectId(LEVEL_ID);
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(publishedLevel));
            Mockito.when(attemptRepository.existsByUserAndLevelAndCompletedTrue(currentUser, publishedLevel))
                    .thenReturn(true);
            Mockito.when(attemptRepository.findFastestGhostCandidate(levelObjectId)).thenReturn(Optional.empty());

            final Optional<GhostDTO> result = ghostService.getGhostForLevel(LEVEL_ID, currentUser);

            Assertions.assertTrue(result.isEmpty());
        }

        /// Verifies that the fastest replayable attempt is mapped to the response DTO.
        @Test
        @DisplayName("returns the fastest available ghost")
        void returnsFastestAvailableGhost() {
            final ObjectId levelObjectId = new ObjectId(LEVEL_ID);
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(publishedLevel));
            Mockito.when(attemptRepository.existsByUserAndLevelAndCompletedTrue(currentUser, publishedLevel))
                    .thenReturn(true);
            Mockito.when(attemptRepository.findFastestGhostCandidate(levelObjectId))
                    .thenReturn(Optional.of(ghostAttempt));

            final GhostDTO result = ghostService.getGhostForLevel(LEVEL_ID, currentUser).orElseThrow();

            Assertions.assertEquals(ATTEMPT_ID, result.attemptId());
            Assertions.assertSame(inputLog, result.inputLog());
            Assertions.assertEquals(12_000L, result.timeTakenMs());
            Assertions.assertEquals(holderUser.getName(), result.holderName());
        }
    }
}
