package ch.usi.inf.bsc.sa4.lab02spring.utils.anticheat;

import ch.usi.inf.bsc.sa4.lab02spring.model.Attempt;
import ch.usi.inf.bsc.sa4.lab02spring.model.AttemptVerificationStatus;
import ch.usi.inf.bsc.sa4.lab02spring.model.InputLogFingerprint;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/// Tests for [AntiCheatSuspicionUtils].
@DisplayName("The Anti Cheat Suspicion Utils")
class AntiCheatSuspicionUtilsTests {

    /// High-complexity fingerprint.
    private static final InputLogFingerprint HIGH_COMPLEXITY = fingerprint(50, 10);

    /// Suspicious exact-match fingerprint.
    private static final InputLogFingerprint SUSPICIOUS_COMPLEXITY = fingerprint(35, 7);

    /// Low-complexity fingerprint.
    private static final InputLogFingerprint LOW_COMPLEXITY = fingerprint(10, 2);

    /// Duplicate attempt used by present optionals.
    private static final Attempt DUPLICATE_ATTEMPT = duplicateAttempt();

    /// Tests for suspicion classification.
    @Nested
    @DisplayName("when classifying fingerprint suspicion")
    class Classification {

        /// Checks that history alone is not enough.
        @Test
        @DisplayName("returns legit when there is no duplicate")
        void returnsLegitWhenThereIsNoDuplicate() {
            final AttemptVerificationStatus result = classify(
                    HIGH_COMPLEXITY,
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    10,
                    10);

            Assertions.assertEquals(AttemptVerificationStatus.LEGIT, result);
        }

        /// Checks high exact duplicates.
        @Test
        @DisplayName("returns cheated for a high exact duplicate")
        void returnsCheatedForHighExactDuplicate() {
            final AttemptVerificationStatus result = classify(
                    HIGH_COMPLEXITY,
                    Optional.of(DUPLICATE_ATTEMPT),
                    Optional.empty(),
                    Optional.empty(),
                    0,
                    0);

            Assertions.assertEquals(AttemptVerificationStatus.CHEATED, result);
        }

        /// Checks suspicious exact duplicates.
        @Test
        @DisplayName("returns suspicious for a medium exact duplicate")
        void returnsSuspiciousForMediumExactDuplicate() {
            final AttemptVerificationStatus result = classify(
                    SUSPICIOUS_COMPLEXITY,
                    Optional.of(DUPLICATE_ATTEMPT),
                    Optional.empty(),
                    Optional.empty(),
                    0,
                    0);

            Assertions.assertEquals(AttemptVerificationStatus.SUSPICIOUS, result);
        }

        /// Checks that history can upgrade suspicious attempts.
        @Test
        @DisplayName("returns cheated when suspicious input has bad history")
        void returnsCheatedWhenSuspiciousInputHasBadHistory() {
            final AttemptVerificationStatus result = classify(
                    SUSPICIOUS_COMPLEXITY,
                    Optional.of(DUPLICATE_ATTEMPT),
                    Optional.empty(),
                    Optional.empty(),
                    0,
                    5);

            Assertions.assertEquals(AttemptVerificationStatus.CHEATED, result);
        }

        /// Checks fuzzy duplicate score.
        @Test
        @DisplayName("returns suspicious for a strong fuzzy duplicate")
        void returnsSuspiciousForStrongFuzzyDuplicate() {
            final AttemptVerificationStatus result = classify(
                    HIGH_COMPLEXITY,
                    Optional.empty(),
                    Optional.of(DUPLICATE_ATTEMPT),
                    Optional.empty(),
                    0,
                    0);

            Assertions.assertEquals(AttemptVerificationStatus.SUSPICIOUS, result);
        }

        /// Checks jitter duplicate score.
        @Test
        @DisplayName("returns cheated for a strong jitter duplicate")
        void returnsCheatedForStrongJitterDuplicate() {
            final AttemptVerificationStatus result = classify(
                    HIGH_COMPLEXITY,
                    Optional.empty(),
                    Optional.empty(),
                    Optional.of(DUPLICATE_ATTEMPT),
                    0,
                    0);

            Assertions.assertEquals(AttemptVerificationStatus.CHEATED, result);
        }

        /// Checks low-complexity duplicates.
        @Test
        @DisplayName("returns legit for a weak duplicate")
        void returnsLegitForWeakDuplicate() {
            final AttemptVerificationStatus result = classify(
                    LOW_COMPLEXITY,
                    Optional.of(DUPLICATE_ATTEMPT),
                    Optional.empty(),
                    Optional.empty(),
                    0,
                    0);

            Assertions.assertEquals(AttemptVerificationStatus.LEGIT, result);
        }
    }

    /// Calls the classifier with shorter test code.
    private static AttemptVerificationStatus classify(
            final InputLogFingerprint fingerprint,
            final Optional<Attempt> exactDuplicate,
            final Optional<Attempt> fuzzyDuplicate,
            final Optional<Attempt> jitterDuplicate,
            final long previousSuspicious,
            final long previousCheated) {
        return AntiCheatSuspicionUtils.classifyFingerprintSuspicion(
                fingerprint,
                exactDuplicate,
                fuzzyDuplicate,
                jitterDuplicate,
                previousSuspicious,
                previousCheated);
    }

    /// Builds a fingerprint with chosen complexity.
    private static InputLogFingerprint fingerprint(final int inputChanges, final int jitterChanges) {
        return new InputLogFingerprint(
                "exact",
                "jitter",
                jitterChanges,
                List.of("bucket"),
                100,
                inputChanges);
    }

    /// Builds a duplicate attempt.
    private static Attempt duplicateAttempt() {
        final User user = new User("user-1", "Mario");
        final Level level = new Level("Level", "desc", user);
        return new Attempt(user, ZonedDateTime.parse("2026-05-20T00:00:00Z"),
                level, true, Duration.ofSeconds(10));
    }
}
