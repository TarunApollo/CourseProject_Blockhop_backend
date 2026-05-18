package ch.usi.inf.bsc.sa4.lab02spring.service.antiCheat;

import java.util.Optional;
import org.springframework.stereotype.Service;

import ch.usi.inf.bsc.sa4.lab02spring.model.Attempt;
import ch.usi.inf.bsc.sa4.lab02spring.model.AttemptVerificationStatus;
import ch.usi.inf.bsc.sa4.lab02spring.model.InputLogFingerprint;

@Service
public class AntiCheatSuspicionService {

    private static final double MAX_SUSPICION = 1.0;
    private static final double NO_SUSPICION = 0.0;
    private static final double CHEAT_THRESHOLD = 0.90;
    private static final double SUSPICION_THRESHOLD = 0.70;
    private static final double EXACT_DUPLICATE_SCORE = 1.0;
    private static final double FUZZY_DUPLICATE_SCORE = 0.80;
    private static final double JITTER_DUPLICATE_SCORE = 0.70;
    private static final double MAX_INPUT_CHANGE_COMPLEXITY = 50.0;
    private static final double MAX_JITTER_INPUT_CHANGE_COMPLEXITY = 10.0;
    private static final double PREVIOUS_SUSPICIOUS_ATTEMPT_SCORE = 0.05;
    private static final double PREVIOUS_CHEATED_ATTEMPT_SCORE = 0.20;


    public AttemptVerificationStatus classifyFingerprintSuspicion(
            InputLogFingerprint fingerprint,
            Optional<Attempt> exactDuplicate,
            Optional<Attempt> fuzzyDuplicate,
            Optional<Attempt> jitterDuplicate,
            long previousSuspiciousLevelsCount,
            long previousCheatedLevelsCount) {
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
                    currentSuspicion,
                    previousSuspiciousLevelsCount,
                    previousCheatedLevelsCount);
        }
        return AttemptVerificationStatus.LEGIT;
    }

    private AttemptVerificationStatus classifySuspiciousWithHistory(
            double currentSuspicion,
            final long previousSuspiciousAttempts,
            final long previousCheatedAttempts) {
        currentSuspicion = PREVIOUS_SUSPICIOUS_ATTEMPT_SCORE;
        if (isCheatingSuspicion(suspicionWithHistory(
                currentSuspicion,
                previousSuspiciousAttempts,
                previousCheatedAttempts))) {
            return AttemptVerificationStatus.CHEATED;
        }
        return AttemptVerificationStatus.SUSPICIOUS;
    }

    private double currentFingerprintSuspicion(final InputLogFingerprint fingerprint,
                                               final Optional<Attempt> exactDuplicate,
                                               final Optional<Attempt> fuzzyDuplicate,
                                               final Optional<Attempt> jitterDuplicate) {
        return Math.max(
                Math.max(
                        calculateExactSuspicion(fingerprint, exactDuplicate),
                        calculateFuzzySuspicion(fingerprint, fuzzyDuplicate)),
                calculateJitterSuspicion(fingerprint, jitterDuplicate));
    }

    private boolean isCheatingSuspicion(final double suspicion) {
        return suspicion >= CHEAT_THRESHOLD;
    }

    private boolean isSuspicious(final double suspicion) {
        return suspicion >= SUSPICION_THRESHOLD;
    }

    private double calculateExactSuspicion(final InputLogFingerprint fingerprint,
                                           final Optional<Attempt> exactDuplicate) {
        final double complexityFactor = complexityFactor(fingerprint);
        return duplicateSuspicion(exactDuplicate, EXACT_DUPLICATE_SCORE, complexityFactor);
    }

    private double calculateFuzzySuspicion(final InputLogFingerprint fingerprint,
                                           final Optional<Attempt> fuzzyDuplicate) {
        final double complexityFactor = complexityFactor(fingerprint);
        return duplicateSuspicion(fuzzyDuplicate, FUZZY_DUPLICATE_SCORE, complexityFactor);
    }

    private double calculateJitterSuspicion(final InputLogFingerprint fingerprint,
                                            final Optional<Attempt> jitterDuplicate) {
        final double complexityFactor = jitterComplexityFactor(fingerprint);
        return duplicateSuspicion(jitterDuplicate, JITTER_DUPLICATE_SCORE, complexityFactor);
    }

    private double duplicateSuspicion(final Optional<Attempt> duplicate,
            final double duplicateScore,
            final double complexityFactor) {
        if (duplicate.isEmpty()) {
            return NO_SUSPICION;
        }

        return duplicateScore * complexityFactor;
    }

    private double recentHistorySuspicion(final long previousSuspiciousAttempts,
                                          final long previousCheatedAttempts) {
        return previousSuspiciousAttempts * PREVIOUS_SUSPICIOUS_ATTEMPT_SCORE
                + previousCheatedAttempts * PREVIOUS_CHEATED_ATTEMPT_SCORE;
    }

    private double suspicionWithHistory(final double currentSuspicion,
                                        final long previousSuspiciousAttempts,
                                        final long previousCheatedAttempts) {
        return Math.min(
                MAX_SUSPICION,
                currentSuspicion + recentHistorySuspicion(previousSuspiciousAttempts, previousCheatedAttempts));
    }

    private double complexityFactor(final InputLogFingerprint fingerprint) {
        return Math.min(MAX_SUSPICION, fingerprint.inputChangeCount() / MAX_INPUT_CHANGE_COMPLEXITY);
    }

    private double jitterComplexityFactor(final InputLogFingerprint fingerprint) {
        return Math.min(MAX_SUSPICION, fingerprint.jitterInputChangeCount() / MAX_JITTER_INPUT_CHANGE_COMPLEXITY);
    }
}
