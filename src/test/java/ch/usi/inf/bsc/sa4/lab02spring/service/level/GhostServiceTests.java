package ch.usi.inf.bsc.sa4.lab02spring.service.level;

import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.InputFrameDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.Attempt;
import ch.usi.inf.bsc.sa4.lab02spring.model.AttemptVerificationStatus;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.repository.AttemptRepository;
import ch.usi.inf.bsc.sa4.lab02spring.repository.LevelRepository;

/// Unit tests for [GhostService].
@SpringBootTest
@DisplayName("The Ghost Service")
class GhostServiceTests {

    /// Fixed timestamp used for ghost attempt fixtures.
    private static final ZonedDateTime NOW =
            ZonedDateTime.of(2026, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC);

    /// Replay input log fixture used by returned ghost attempts.
    private static final List<InputFrameDTO> SAMPLE_LOG = List.of(
            new InputFrameDTO(1, true, false, false, false));

    /// The service under test.
    @Autowired
    private GhostService ghostService;

    /// Mocked repository used to resolve published levels.
    @MockitoBean
    private LevelRepository levelRepository;

    /// Mocked repository used to resolve unlock and ghost attempt queries.
    @MockitoBean
    private AttemptRepository attemptRepository;

    /// Current user requesting the ghost fixture.
    private User currentUser;

    /// Published level fixture used by the service.
    private Level publishedLevel;

    /// Sets up test data before each test.
    @BeforeEach
    void setUp() {
        final User creator = new User("creator-1", "Mario");
        currentUser = new User("user-1", "Luigi");
        publishedLevel = new Level("Ghost Level", "desc", creator);
        ReflectionTestUtils.setField(publishedLevel, "id", "64b64c9f6f4b2c0012345678");
        ReflectionTestUtils.setField(publishedLevel, "published", Boolean.TRUE);
    }

    /// Verifies that a user without a legit completion cannot unlock a ghost.
    @Test
    @DisplayName("returns empty when the current user has no LEGIT completion on the level")
    void requiresLegitCompletionToUnlockGhost() {
        Mockito.when(levelRepository.findById(publishedLevel.getId())).thenReturn(Optional.of(publishedLevel));
        Mockito.when(attemptRepository.existsByUserAndLevelAndCompletedTrueAndAntiCheatStatus(
                currentUser,
                publishedLevel,
                AttemptVerificationStatus.LEGIT)).thenReturn(Boolean.FALSE);

        final Optional<?> result = ghostService.getGhostForLevel(publishedLevel.getId(), currentUser);

        Assertions.assertTrue(result.isEmpty());
        Mockito.verify(attemptRepository, Mockito.never())
                .findFastestVerifiedGhostCandidate(Mockito.any(), Mockito.any());
    }

    /// Verifies that the fastest legit ghost candidate is returned when unlocked.
    @Test
    @DisplayName("returns the fastest LEGIT ghost candidate")
    void returnsFastestLegitCandidate() {
        final Attempt legitAttempt = new Attempt(
                currentUser,
                NOW,
                publishedLevel,
                true,
                Duration.ofSeconds(12));
        legitAttempt.setAntiCheatStatus(AttemptVerificationStatus.LEGIT);
        legitAttempt.setInputLog(SAMPLE_LOG);
        ReflectionTestUtils.setField(legitAttempt, "id", "attempt-1");

        Mockito.when(levelRepository.findById(publishedLevel.getId())).thenReturn(Optional.of(publishedLevel));
        Mockito.when(attemptRepository.existsByUserAndLevelAndCompletedTrueAndAntiCheatStatus(
                currentUser,
                publishedLevel,
                AttemptVerificationStatus.LEGIT)).thenReturn(Boolean.TRUE);
        Mockito.when(attemptRepository.findFastestVerifiedGhostCandidate(
                new ObjectId(publishedLevel.getId()),
                AttemptVerificationStatus.LEGIT)).thenReturn(Optional.of(legitAttempt));

        final Optional<?> result = ghostService.getGhostForLevel(publishedLevel.getId(), currentUser);

        Assertions.assertTrue(result.isPresent());
        Mockito.verify(attemptRepository).findFastestVerifiedGhostCandidate(
                new ObjectId(publishedLevel.getId()),
                AttemptVerificationStatus.LEGIT);
    }
}
