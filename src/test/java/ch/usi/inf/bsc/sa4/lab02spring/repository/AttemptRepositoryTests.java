package ch.usi.inf.bsc.sa4.lab02spring.repository;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.InputFrameDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.Attempt;
import ch.usi.inf.bsc.sa4.lab02spring.model.AttemptVerificationStatus;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

/// Integration tests for the ghost-replay query methods on [AttemptRepository].
///
/// Runs against the embedded MongoDB provided by flapdoodle.
/// Each test method starts from a clean slate — repositories are wiped in setup.
@SpringBootTest
@DisplayName("AttemptRepository ghost-replay queries")
@SuppressWarnings("NullAway")
/* package */ class AttemptRepositoryTests {

    /// Fixed timestamp shared across all test attempts.
    private static final ZonedDateTime NOW =
            ZonedDateTime.of(2026, 5, 20, 0, 0, 0, 0, ZoneOffset.UTC);

    /// Minimal non-empty input log used as the ghost-replay payload in fixtures.
    private static final List<InputFrameDTO> SAMPLE_LOG = List.of(
            new InputFrameDTO(1, true, false, false, false),
            new InputFrameDTO(2, false, false, true, false));

    /// Repository under test.
    @Autowired
    private AttemptRepository attemptRepository;

    /// Used to persist User fixtures required by Attempt and Level.
    @Autowired
    private UserRepository userRepository;

    /// Used to persist Level fixtures required by Attempt.
    @Autowired
    private LevelRepository levelRepository;

    /// Shared user fixture, recreated before each test.
    private User user;

    /// Shared level fixture, recreated before each test.
    private Level level;

    /// Wipes all collections and rebuilds the shared user and level fixtures.
    @BeforeEach
    /* package */ void setup() {
        attemptRepository.deleteAll();
        levelRepository.deleteAll();
        userRepository.deleteAll();

        user = userRepository.save(new User("user-1", "Mario"));
        level = levelRepository.save(new Level("Test Level", "desc", user));
    }

    /// Returns the ObjectId of the shared level fixture.
    private ObjectId levelId() {
        return new ObjectId(level.getId());
    }

    /// Saves a completed attempt with a non-empty input log for the shared level.
    ///
    /// @param timeTaken how long the attempt took
    /// @return the persisted attempt
    private Attempt completedWithLog(final Duration timeTaken) {
        final Attempt attempt = new Attempt(user, NOW, level, true, timeTaken);
        attempt.setInputLog(SAMPLE_LOG);
        return attemptRepository.save(attempt);
    }

    /// Saves a completed attempt with a non-empty input log and explicit status.
    ///
    /// @param timeTaken how long the attempt took
    /// @param status    anti-cheat status to assign
    /// @return the persisted attempt
    private Attempt completedWithLog(final Duration timeTaken, final AttemptVerificationStatus status) {
        final Attempt attempt = new Attempt(user, NOW, level, true, timeTaken);
        attempt.setInputLog(SAMPLE_LOG);
        attempt.setAntiCheatStatus(status);
        return attemptRepository.save(attempt);
    }

    // -----------------------------------------------------------------------
    // existsByUserAndLevelAndCompletedTrue
    // -----------------------------------------------------------------------

    /// Tests for [AttemptRepository#existsByUserAndLevelAndCompletedTrue].
    @Nested
    @DisplayName("existsByUserAndLevelAndCompletedTrue")
    /* package */ class ExistsByUserAndLevelAndCompletedTrue {

        /// Verifies that no attempts results in false.
        @Test
        @DisplayName("returns false when the user has no attempts on the level")
        /* package */ void noAttempts() {
            Assertions.assertFalse(
                    attemptRepository.existsByUserAndLevelAndCompletedTrue(user, level));
        }

        /// Verifies that only non-completed attempts results in false.
        @Test
        @DisplayName("returns false when the user has only non-completed attempts")
        /* package */ void onlyIncomplete() {
            attemptRepository.save(new Attempt(user, NOW, level, false, Duration.ofSeconds(10)));

            Assertions.assertFalse(
                    attemptRepository.existsByUserAndLevelAndCompletedTrue(user, level));
        }

        /// Verifies that at least one completed attempt results in true.
        @Test
        @DisplayName("returns true when the user has at least one completed attempt")
        /* package */ void hasCompleted() {
            attemptRepository.save(new Attempt(user, NOW, level, true, Duration.ofSeconds(10)));

            Assertions.assertTrue(
                    attemptRepository.existsByUserAndLevelAndCompletedTrue(user, level));
        }
    }

    // -----------------------------------------------------------------------
    // findFastestGhostCandidate
    // -----------------------------------------------------------------------

    /// Tests for [AttemptRepository#findFastestGhostCandidate].
    @Nested
    @DisplayName("findFastestGhostCandidate")
    /* package */ class FindFastestGhostCandidateTests {

        /// Verifies empty result when no attempts exist.
        @Test
        @DisplayName("returns empty when no attempts exist for the level")
        /* package */ void noAttempts() {
            final Optional<Attempt> result = attemptRepository.findFastestGhostCandidate(levelId());

            Assertions.assertTrue(result.isEmpty());
        }

        /// Verifies empty result when all attempts have a null or empty inputLog.
        @Test
        @DisplayName("returns empty when all attempts have a null or empty inputLog")
        /* package */ void noUsableLog() {
            attemptRepository.save(new Attempt(user, NOW, level, true, Duration.ofSeconds(10)));

            final Attempt emptyLog = new Attempt(user, NOW, level, true, Duration.ofSeconds(5));
            emptyLog.setInputLog(List.of());
            attemptRepository.save(emptyLog);

            final Optional<Attempt> result = attemptRepository.findFastestGhostCandidate(levelId());

            Assertions.assertTrue(result.isEmpty());
        }

        /// Verifies empty result when only non-completed attempts carry a log.
        @Test
        @DisplayName("returns empty when only non-completed attempts have a log")
        /* package */ void onlyIncompleteHaveLog() {
            final Attempt incomplete = new Attempt(user, NOW, level, false, Duration.ofSeconds(10));
            incomplete.setInputLog(SAMPLE_LOG);
            attemptRepository.save(incomplete);

            final Optional<Attempt> result = attemptRepository.findFastestGhostCandidate(levelId());

            Assertions.assertTrue(result.isEmpty());
        }

        /// Verifies that the attempt with the smallest timeTaken is returned.
        @Test
        @DisplayName("returns the attempt with the smallest timeTaken among qualifying attempts")
        /* package */ void returnsFastest() {
            completedWithLog(Duration.ofSeconds(30));
            final Attempt fastest = completedWithLog(Duration.ofSeconds(10));
            completedWithLog(Duration.ofSeconds(20));

            final Optional<Attempt> result = attemptRepository.findFastestGhostCandidate(levelId());

            Assertions.assertTrue(result.isPresent());
            Assertions.assertEquals(fastest.getId(), result.get().getId());
        }

        /// Verifies that single-digit and double-digit second durations sort by
        /// actual elapsed time, not lexicographic string order.
        @Test
        @DisplayName("returns 9s ahead of 10s when durations cross a digit boundary")
        /* package */ void returnsFastestAcrossDigitBoundary() {
            completedWithLog(Duration.ofSeconds(10));
            final Attempt fastest = completedWithLog(Duration.ofSeconds(9));

            final Optional<Attempt> result = attemptRepository.findFastestGhostCandidate(levelId());

            Assertions.assertTrue(result.isPresent());
            Assertions.assertEquals(fastest.getId(), result.get().getId());
        }

        /// Verifies that pre-timeTakenMs attempts are ignored once legacy
        /// fallback support is removed.
        @Test
        @DisplayName("ignores legacy attempts that do not have timeTakenMs")
        /* package */ void ignoresLegacyAttemptsWithoutNumericDuration() {
            final Attempt legacyAttempt = new Attempt(user, NOW, level, true, Duration.ofSeconds(8));
            legacyAttempt.setInputLog(SAMPLE_LOG);
            ReflectionTestUtils.setField(legacyAttempt, "timeTakenMs", null);
            attemptRepository.save(legacyAttempt);

            final Optional<Attempt> result = attemptRepository.findFastestGhostCandidate(levelId());

            Assertions.assertTrue(result.isEmpty());
        }
    }

    // -----------------------------------------------------------------------
    // findFastestVerifiedGhostCandidate
    // -----------------------------------------------------------------------

    /// Tests for [AttemptRepository#findFastestVerifiedGhostCandidate].
    @Nested
    @DisplayName("findFastestVerifiedGhostCandidate")
    /* package */ class FindFastestVerifiedGhostCandidateTests {

        /// Verifies empty result when no attempts exist.
        @Test
        @DisplayName("returns empty when no attempts exist for the level")
        /* package */ void noAttempts() {
            final Optional<Attempt> result = attemptRepository.findFastestVerifiedGhostCandidate(
                    levelId(), AttemptVerificationStatus.LEGIT);

            Assertions.assertTrue(result.isEmpty());
        }

        /// Verifies empty result when all attempts have a null or empty inputLog.
        @Test
        @DisplayName("returns empty when all attempts have a null or empty inputLog")
        /* package */ void noUsableLog() {
            final Attempt noLog = new Attempt(user, NOW, level, true, Duration.ofSeconds(10));
            noLog.setAntiCheatStatus(AttemptVerificationStatus.LEGIT);
            attemptRepository.save(noLog);

            final Optional<Attempt> result = attemptRepository.findFastestVerifiedGhostCandidate(
                    levelId(), AttemptVerificationStatus.LEGIT);

            Assertions.assertTrue(result.isEmpty());
        }

        /// Verifies empty result when only non-completed attempts carry a log.
        @Test
        @DisplayName("returns empty when only non-completed attempts have a log")
        /* package */ void onlyIncompleteHaveLog() {
            final Attempt incomplete = new Attempt(user, NOW, level, false, Duration.ofSeconds(10));
            incomplete.setInputLog(SAMPLE_LOG);
            incomplete.setAntiCheatStatus(AttemptVerificationStatus.LEGIT);
            attemptRepository.save(incomplete);

            final Optional<Attempt> result = attemptRepository.findFastestVerifiedGhostCandidate(
                    levelId(), AttemptVerificationStatus.LEGIT);

            Assertions.assertTrue(result.isEmpty());
        }

        /// Verifies that the fastest attempt with the requested status is returned.
        @Test
        @DisplayName("returns the fastest attempt with the given status")
        /* package */ void returnsFastest() {
            completedWithLog(Duration.ofSeconds(30), AttemptVerificationStatus.LEGIT);
            final Attempt fastest = completedWithLog(Duration.ofSeconds(10), AttemptVerificationStatus.LEGIT);
            completedWithLog(Duration.ofSeconds(20), AttemptVerificationStatus.LEGIT);

            final Optional<Attempt> result = attemptRepository.findFastestVerifiedGhostCandidate(
                    levelId(), AttemptVerificationStatus.LEGIT);

            Assertions.assertTrue(result.isPresent());
            Assertions.assertEquals(fastest.getId(), result.get().getId());
        }

        /// Verifies that CHEATED attempts are excluded even when faster than LEGIT ones.
        @Test
        @DisplayName("excludes CHEATED attempts even when they are faster than LEGIT ones")
        /* package */ void excludesCheated() {
            completedWithLog(Duration.ofSeconds(5), AttemptVerificationStatus.CHEATED);
            final Attempt legit = completedWithLog(Duration.ofSeconds(20), AttemptVerificationStatus.LEGIT);

            final Optional<Attempt> result = attemptRepository.findFastestVerifiedGhostCandidate(
                    levelId(), AttemptVerificationStatus.LEGIT);

            Assertions.assertTrue(result.isPresent());
            Assertions.assertEquals(legit.getId(), result.get().getId());
        }

        /// Verifies that verified pre-timeTakenMs attempts are ignored once
        /// legacy fallback support is removed.
        @Test
        @DisplayName("ignores verified legacy attempts that do not have timeTakenMs")
        /* package */ void ignoresVerifiedLegacyAttemptsWithoutNumericDuration() {
            final Attempt legacyAttempt = new Attempt(user, NOW, level, true, Duration.ofSeconds(8));
            legacyAttempt.setInputLog(SAMPLE_LOG);
            legacyAttempt.setAntiCheatStatus(AttemptVerificationStatus.LEGIT);
            ReflectionTestUtils.setField(legacyAttempt, "timeTakenMs", null);
            attemptRepository.save(legacyAttempt);

            final Optional<Attempt> result = attemptRepository.findFastestVerifiedGhostCandidate(
                    levelId(), AttemptVerificationStatus.LEGIT);

            Assertions.assertTrue(result.isEmpty());
        }
    }
}
