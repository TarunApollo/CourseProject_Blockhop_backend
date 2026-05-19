package ch.usi.inf.bsc.sa4.lab02spring.utils;

import java.util.Optional;

import ch.usi.inf.bsc.sa4.lab02spring.model.Attempt;
import ch.usi.inf.bsc.sa4.lab02spring.model.AttemptVerificationStatus;
import ch.usi.inf.bsc.sa4.lab02spring.model.InputLogFingerprint;

/// Classifies input fingerprint matches into anti-cheat verification statuses.
public final class AntiCheatSuspicionUtils {

    /// Highest suspicion score assigned by the classifier.
    private static final double MAX_SUSPICION = 1.0;
    /// Suspicion score used when no duplicate evidence exists.
    private static final double NO_SUSPICION = 0.0;
    /// Minimum score that marks an attempt as cheated.
    private static final double CHEAT_THRESHOLD = 0.90;
    /// Minimum score that marks an attempt as suspicious.
    private static final double SUSP_THRESHOLD = 0.70;
    /// Score assigned to an exact duplicate fingerprint match.
    private static final double EXACT_DUP_SCORE = 1.0;
    /// Score assigned to a fuzzy duplicate fingerprint match.
    private static final double FUZZY_DUP_SCORE = 0.80;
    /// Score assigned to a jitter-normalized duplicate fingerprint match.
    private static final double JITTER_DUP_SCORE = 1.0;
    /// Input-change count that reaches full complexity for exact/fuzzy checks.
    private static final double MAX_CHANGE_CPLX = 50.0;
    /// Input-change count that reaches full complexity for jitter checks.
    private static final double MAX_JITTER_CPLX = 10.0;
    /// Suspicion added for each previous suspicious attempt.
    private static final double PREV_SUSP_SCORE = 0.05;
    /// Suspicion added for each previous cheated attempt.
    private static final double PREV_CHEAT_SCORE = 0.20;

    /// Prevents construction of this utility class.
    private AntiCheatSuspicionUtils() {
    }

    /// Classifies an attempt from duplicate evidence and recent player history.
    /// 
    /// @param fingerprint                   the fingerprint generated for the
    ///                                      current attempt
    /// @param exactDuplicate                an existing attempt with the same exact
    ///                                      fingerprint
    /// @param fuzzyDuplicate                an existing attempt with a similar fuzzy
    ///                                      fingerprint
    /// @param jitterDuplicate               an existing jitter-normalized duplicate
    ///                                      attempt
    /// @param prevSuspLevels suspicious recent attempts to count
    /// @param prevCheatLevels cheated recent attempts to count
    /// @return the anti-cheat verification status for the attempt
    public static AttemptVerificationStatus classifyFingerprintSuspicion(
            InputLogFingerprint fingerprint,
            Optional<Attempt> exactDuplicate,
            Optional<Attempt> fuzzyDuplicate,
            Optional<Attempt> jitterDuplicate,
            long prevSuspLevels,
            long prevCheatLevels) {
        final double currentSuspicion = currentFingerprintSuspicion(
                fingerprint,
                exactDuplicate,
                fuzzyDuplicate,
                jitterDuplicate);

        if (isCheatingSuspicion(currentSuspicion)) {
            return AttemptVerificationStatus.CHEATED;
        }

        if (isSuspicious(currentSuspicion)) {
            return classifySuspiciousWithHistory(
                    prevSuspLevels,
                    prevCheatLevels);
        }
        return AttemptVerificationStatus.LEGIT;
    }

    private static AttemptVerificationStatus classifySuspiciousWithHistory(
            final long prevSuspAttempts,
            final long prevCheatAttempts) {
        if (isCheatingSuspicion(suspicionWithHistory(
                prevSuspAttempts,
                prevCheatAttempts))) {
            return AttemptVerificationStatus.CHEATED;
        }
        return AttemptVerificationStatus.SUSPICIOUS;
    }

    private static double currentFingerprintSuspicion(final InputLogFingerprint fingerprint,
            final Optional<Attempt> exactDuplicate,
            final Optional<Attempt> fuzzyDuplicate,
            final Optional<Attempt> jitterDuplicate) {
        return Math.max(
                Math.max(
                        calculateExactSuspicion(fingerprint, exactDuplicate),
                        calculateFuzzySuspicion(fingerprint, fuzzyDuplicate)),
                calculateJitterSuspicion(fingerprint, jitterDuplicate));
    }

    private static boolean isCheatingSuspicion(final double suspicion) {
        return suspicion >= CHEAT_THRESHOLD;
    }

    private static boolean isSuspicious(final double suspicion) {
        return suspicion >= SUSP_THRESHOLD;
    }

    private static double calculateExactSuspicion(final InputLogFingerprint fingerprint,
            final Optional<Attempt> exactDuplicate) {
        final double complexityFactor = complexityFactor(fingerprint);
        return duplicateSuspicion(exactDuplicate, EXACT_DUP_SCORE, complexityFactor);
    }

    private static double calculateFuzzySuspicion(final InputLogFingerprint fingerprint,
            final Optional<Attempt> fuzzyDuplicate) {
        final double complexityFactor = complexityFactor(fingerprint);
        return duplicateSuspicion(fuzzyDuplicate, FUZZY_DUP_SCORE, complexityFactor);
    }

    private static double calculateJitterSuspicion(final InputLogFingerprint fingerprint,
            final Optional<Attempt> jitterDuplicate) {
        final double complexityFactor = jitterComplexityFactor(fingerprint);
        return duplicateSuspicion(jitterDuplicate, JITTER_DUP_SCORE, complexityFactor);
    }

    private static double duplicateSuspicion(final Optional<Attempt> duplicate,
            final double duplicateScore,
            final double complexityFactor) {
        if (duplicate.isEmpty()) {
            return NO_SUSPICION;
        }

        return duplicateScore * complexityFactor;
    }

    private static double recentHistorySuspicion(final long prevSuspAttempts,
            final long prevCheatAttempts) {
        return prevSuspAttempts * PREV_SUSP_SCORE
                + prevCheatAttempts * PREV_CHEAT_SCORE;
    }

    private static double suspicionWithHistory(
            final long prevSuspAttempts,
            final long prevCheatAttempts) {
        return Math.min(
                MAX_SUSPICION,
                PREV_SUSP_SCORE
                        + recentHistorySuspicion(prevSuspAttempts, prevCheatAttempts));
    }

    private static double complexityFactor(final InputLogFingerprint fingerprint) {
        return Math.min(MAX_SUSPICION, fingerprint.inputChangeCount() / MAX_CHANGE_CPLX);
    }

    private static double jitterComplexityFactor(final InputLogFingerprint fingerprint) {
        return Math.min(MAX_SUSPICION, fingerprint.jitterInputChangeCount() / MAX_JITTER_CPLX);
    }
}
