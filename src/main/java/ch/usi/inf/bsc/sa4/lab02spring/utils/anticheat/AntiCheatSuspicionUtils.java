package ch.usi.inf.bsc.sa4.lab02spring.utils.anticheat;

import java.util.Optional;

import ch.usi.inf.bsc.sa4.lab02spring.model.Attempt;
import ch.usi.inf.bsc.sa4.lab02spring.model.AttemptVerificationStatus;
import ch.usi.inf.bsc.sa4.lab02spring.model.InputLogFingerprint;

/// Turns input matches into anti-cheat statuses.
public final class AntiCheatSuspicionUtils {

    /// Highest suspicion score.
    private static final double MAX_SUSPICION = 1.0;
    /// Score when there is no duplicate.
    private static final double NO_SUSPICION = 0.0;
    /// Minimum score for cheated.
    private static final double CHEAT_THRESHOLD = 0.90;
    /// Minimum score for suspicious.
    private static final double SUSP_THRESHOLD = 0.70;
    /// Score for the same exact input.
    private static final double EXACT_DUP_SCORE = 1.0;
    /// Score for a close bucket match.
    private static final double FUZZY_DUP_SCORE = 0.80;
    /// Score for the same jitter-cleaned input.
    private static final double JITTER_DUP_SCORE = 1.0;
    /// Input changes needed for full exact/bucket score.
    private static final double MAX_CHANGE_CPLX = 50.0;
    /// Input changes needed for full jitter score.
    private static final double MAX_JITTER_CPLX = 10.0;
    /// Score added for each past suspicious attempt.
    private static final double PREV_SUSP_SCORE = 0.05;
    /// Score added for each past cheated attempt.
    private static final double PREV_CHEAT_SCORE = 0.20;

    /// Do not create this helper.
    private AntiCheatSuspicionUtils() {
    }

    /// Picks a status from duplicate matches and player history.
    ///
    /// @param fingerprint input hashes for this attempt
    /// @param exactDuplicate another attempt with the same exact input
    /// @param fuzzyDuplicate another attempt with a close bucket match
    /// @param jitterDuplicate another attempt with the same jitter-cleaned input
    /// @param prevSuspLevels recent suspicious attempts by this player
    /// @param prevCheatLevels recent cheated attempts by this player
    /// @return status for the attempt
    public static AttemptVerificationStatus classifyFingerprintSuspicion(
            final InputLogFingerprint fingerprint,
            final Optional<Attempt> exactDuplicate,
            final Optional<Attempt> fuzzyDuplicate,
            final Optional<Attempt> jitterDuplicate,
            final long prevSuspLevels,
            final long prevCheatLevels) {
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
