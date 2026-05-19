package ch.usi.inf.bsc.sa4.lab02spring.service.anticheat;

import ch.usi.inf.bsc.sa4.lab02spring.model.Attempt;
import ch.usi.inf.bsc.sa4.lab02spring.model.AttemptVerificationStatus;
import ch.usi.inf.bsc.sa4.lab02spring.model.InputLogFingerprint;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.service.AttemptService;
import ch.usi.inf.bsc.sa4.lab02spring.utils.anticheat.AntiCheatSuspicionUtils;

import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.Optional;

/// Classifies fingerprint-based suspicion by checking for duplicate fingerprints
/// and previous suspicious or cheated attempts within a recent time window.
@Service
public class FingerprintSuspicionService {

    /// Number of days considered when looking for recent suspicious attempts.
    private static final int RECENT_ATTEMPT_WINDOW_DAYS = 7;

    private final AttemptService attemptService;

    /// Constructs the service with its attempt lookup dependency.
    ///
    /// @param attemptService service that provides attempt queries
    public FingerprintSuspicionService(final AttemptService attemptService) {
        this.attemptService = attemptService;
    }

    /// Classifies the suspicion level of a fingerprint by comparing it against
    /// known duplicates and recent attempt history.
    ///
    /// @param level       the level the attempt belongs to
    /// @param user        the player who made the attempt
    /// @param attemptId   the current attempt identifier
    /// @param fingerprint the input log fingerprint to classify
    /// @return the computed verification status based on duplicate and history analysis
    public AttemptVerificationStatus classify(final Level level,
            final User user,
            final String attemptId,
            final InputLogFingerprint fingerprint) {
        final Optional<Attempt> exactDuplicate = attemptService.findExactFingerprintDuplicate(level, attemptId,
                fingerprint);
        final Optional<Attempt> fuzzyDuplicate = attemptService.findFuzzyFingerprintDuplicate(level, attemptId,
                fingerprint);
        final Optional<Attempt> jitterDuplicate = attemptService.findJitterFingerprintDuplicate(level, attemptId,
                fingerprint);
        final ZonedDateTime recentAttemptWindowStart = ZonedDateTime.now().minusDays(RECENT_ATTEMPT_WINDOW_DAYS);
        final long previousSuspiciousAttempts = attemptService.countAttemptsByLevelUserStatusAfter(
                level,
                user,
                AttemptVerificationStatus.SUSPICIOUS,
                recentAttemptWindowStart,
                attemptId);
        final long previousCheatedAttempts = attemptService.countAttemptsByLevelUserStatusAfter(
                level,
                user,
                AttemptVerificationStatus.CHEATED,
                recentAttemptWindowStart,
                attemptId);
        return AntiCheatSuspicionUtils.classifyFingerprintSuspicion(
                fingerprint,
                exactDuplicate,
                fuzzyDuplicate,
                jitterDuplicate,
                previousSuspiciousAttempts,
                previousCheatedAttempts);
    }
}
