package ch.usi.inf.bsc.sa4.lab02spring.service.antiCheat;

import static ch.usi.inf.bsc.sa4.lab02spring.utils.AnsiLogHelper.bold;
import static ch.usi.inf.bsc.sa4.lab02spring.utils.AnsiLogHelper.cyan;
import static ch.usi.inf.bsc.sa4.lab02spring.utils.AnsiLogHelper.green;
import static ch.usi.inf.bsc.sa4.lab02spring.utils.AnsiLogHelper.red;
import static ch.usi.inf.bsc.sa4.lab02spring.utils.AnsiLogHelper.yellow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/// Centralized logger for anti-cheat replay and verification events.
public final class AntiCheatLog {

    /// Logger used for all anti-cheat messages.
    private static final Logger log = LoggerFactory.getLogger(AntiCheatLog.class);

    /// Prevents instantiation of this utility class.
    private AntiCheatLog() {
    }

    /// Logs that a player has started a level attempt.
    /// @param userId the id of the player who started the level
    /// @param levelId the id of the level that was started
    public static void levelStarted(final String userId, final String levelId) {
        log.info("{} {} {} started level {}",
                cyan("[ANTICHEAT]"), bold("Player"), userId, levelId);
    }

    /// Logs that a player has opened a level for play.
    /// @param userId the id of the player who opened the level
    /// @param levelId the id of the opened level
    public static void levelEntered(final String userId, final String levelId) {
        log.info("{} Player {} opened level {} for play",
                cyan("[ANTICHEAT]"), userId, levelId);
    }

    /// Logs that a player completed a level and replay validation will start.
    /// @param userId the id of the player who completed the level
    /// @param levelId the id of the completed level
    /// @param frames the number of recorded frames in the attempt
    public static void levelCompleted(final String userId, final String levelId, final int frames) {
        log.info("{} {} {} completed level {} ({} frames), starting replay...",
                cyan("[ANTICHEAT]"), bold("Player"), userId, levelId, frames);
    }

    /// Logs a replay that was verified as valid.
    /// @param userId the id of the player whose replay was verified
    /// @param levelId the id of the replayed level
    /// @param reason the validation reason to include in the log
    public static void replayValid(final String userId, final String levelId, final String reason) {
        log.info("{} {} for player {} on level {}: {}",
                green("[ANTICHEAT]"), bold("REPLAY VERIFIED"), userId, levelId, reason);
    }

    /// Logs a replay that did not match the submitted attempt.
    /// @param userId the id of the player whose replay mismatched
    /// @param levelId the id of the replayed level
    /// @param reason the mismatch reason to include in the log
    public static void replayMismatch(final String userId, final String levelId, final String reason) {
        log.warn("{} {} for player {} on level {}: {}",
                red("[ANTICHEAT]"), bold("REPLAY MISMATCH"), userId, levelId, reason);
    }

    /// Logs a replay that passed validation but remains suspicious.
    /// @param userId the id of the player whose replay was suspicious
    /// @param levelId the id of the replayed level
    /// @param reason the suspicion reason to include in the log
    public static void replaySuspicious(final String userId, final String levelId, final String reason) {
        log.warn("{} {} for player {} on level {}: {}",
                yellow("[ANTICHEAT]"), bold("REPLAY SUSPICIOUS"), userId, levelId, reason);
    }

    /// Logs a replay that was rejected as invalid.
    /// @param userId the id of the player whose replay was invalid
    /// @param levelId the id of the replayed level
    /// @param reason the invalid replay reason to include in the log
    public static void replayInvalid(final String userId, final String levelId, final String reason) {
        log.warn("{} {} for player {} on level {}: {}",
                red("[ANTICHEAT]"), bold("REPLAY INVALID"), userId, levelId, reason);
    }

    /// Logs a replay execution failure.
    /// @param userId the id of the player whose replay failed
    /// @param levelId the id of the replayed level
    /// @param error the error message to include in the log
    public static void replayError(final String userId, final String levelId, final String error) {
        log.error("{} Replay FAILED for player {} on level {}: {}",
                red("[ANTICHEAT]"), userId, levelId, error);
    }

    /// Logs that replay execution timed out.
    /// @param userId the id of the player whose replay timed out
    /// @param levelId the id of the replayed level
    public static void replayTimeout(final String userId, final String levelId) {
        log.warn("{} Replay TIMED OUT for player {} on level {}",
                yellow("[ANTICHEAT]"), userId, levelId);
    }

    /// Logs the replay command before starting the headless replay process.
    /// @param userId the id of the player whose replay is starting
    /// @param levelId the id of the level being replayed
    /// @param command the replay command that will be executed
    public static void replaySpinningUp(final String userId, final String levelId, final String command) {
        log.info("{} {} for player {} on level {} via `{}`",
                cyan("[ANTICHEAT]"), bold("Spinning up headless Phaser"), userId, levelId, command);
    }
}
