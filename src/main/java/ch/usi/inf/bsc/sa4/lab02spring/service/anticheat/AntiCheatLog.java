package ch.usi.inf.bsc.sa4.lab02spring.service.anticheat;

import static ch.usi.inf.bsc.sa4.lab02spring.utils.AnsiLogHelper.bold;
import static ch.usi.inf.bsc.sa4.lab02spring.utils.AnsiLogHelper.cyan;
import static ch.usi.inf.bsc.sa4.lab02spring.utils.AnsiLogHelper.green;
import static ch.usi.inf.bsc.sa4.lab02spring.utils.AnsiLogHelper.red;
import static ch.usi.inf.bsc.sa4.lab02spring.utils.AnsiLogHelper.yellow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/// Logs anti-cheat events.
@SuppressWarnings("PMD.TooManyStaticImports")
public final class AntiCheatLog {

    /// Anti-cheat logger.
    private static final Logger LOG = LoggerFactory.getLogger(AntiCheatLog.class);

    /// Text at the start of each anti-cheat log.
    private static final String LOG_PREFIX = "[ANTICHEAT]";

    /// Shared replay result log format.
    private static final String REPLAY_OUTCOME_FORMAT = "{} {} for player {} on level {}: {}";

    /// Do not create this helper.
    private AntiCheatLog() {
    }

    /// Logs that a player started a level.
    /// @param userId player id
    /// @param levelId level id
    public static void levelStarted(final String userId, final String levelId) {
        if (LOG.isInfoEnabled()) {
            LOG.info("{} {} {} started level {}",
                    cyan(LOG_PREFIX), bold("Player"), userId, levelId);
        }
    }

    /// Logs that a player opened a level.
    /// @param userId player id
    /// @param levelId level id
    public static void levelEntered(final String userId, final String levelId) {
        if (LOG.isInfoEnabled()) {
            LOG.info("{} Player {} opened level {} for play",
                    cyan(LOG_PREFIX), userId, levelId);
        }
    }

    /// Logs that a player completed a level.
    /// @param userId player id
    /// @param levelId level id
    /// @param frames recorded frame count
    public static void levelCompleted(final String userId, final String levelId, final int frames) {
        if (LOG.isInfoEnabled()) {
            LOG.info("{} {} {} completed level {} ({} frames), starting replay...",
                    cyan(LOG_PREFIX), bold("Player"), userId, levelId, frames);
        }
    }

    /// Logs a valid replay.
    /// @param userId player id
    /// @param levelId level id
    /// @param reason replay result reason
    public static void replayValid(final String userId, final String levelId, final String reason) {
        if (LOG.isInfoEnabled()) {
            LOG.info(REPLAY_OUTCOME_FORMAT,
                    green(LOG_PREFIX), bold("REPLAY VERIFIED"), userId, levelId, reason);
        }
    }

    /// Logs a replay mismatch.
    /// @param userId player id
    /// @param levelId level id
    /// @param reason mismatch reason
    public static void replayMismatch(final String userId, final String levelId, final String reason) {
        if (LOG.isWarnEnabled()) {
            LOG.warn(REPLAY_OUTCOME_FORMAT,
                    red(LOG_PREFIX), bold("REPLAY MISMATCH"), userId, levelId, reason);
        }
    }

    /// Logs a replay that passed but is suspicious.
    /// @param userId player id
    /// @param levelId level id
    /// @param reason suspicion reason
    public static void replaySuspicious(final String userId, final String levelId, final String reason) {
        if (LOG.isWarnEnabled()) {
            LOG.warn(REPLAY_OUTCOME_FORMAT,
                    yellow(LOG_PREFIX), bold("REPLAY SUSPICIOUS"), userId, levelId, reason);
        }
    }

    /// Logs an invalid replay.
    /// @param userId player id
    /// @param levelId level id
    /// @param reason invalid replay reason
    public static void replayInvalid(final String userId, final String levelId, final String reason) {
        if (LOG.isWarnEnabled()) {
            LOG.warn(REPLAY_OUTCOME_FORMAT,
                    red(LOG_PREFIX), bold("REPLAY INVALID"), userId, levelId, reason);
        }
    }

    /// Logs a replay error.
    /// @param userId player id
    /// @param levelId level id
    /// @param error error message
    public static void replayError(final String userId, final String levelId, final String error) {
        if (LOG.isErrorEnabled()) {
            LOG.error("{} Replay FAILED for player {} on level {}: {}",
                    red(LOG_PREFIX), userId, levelId, error);
        }
    }

    /// Logs a replay timeout.
    /// @param userId player id
    /// @param levelId level id
    public static void replayTimeout(final String userId, final String levelId) {
        if (LOG.isWarnEnabled()) {
            LOG.warn("{} Replay TIMED OUT for player {} on level {}",
                    yellow(LOG_PREFIX), userId, levelId);
        }
    }

    /// Logs the replay command before it starts.
    /// @param userId player id
    /// @param levelId level id
    /// @param command command to run
    public static void replaySpinningUp(final String userId, final String levelId, final String command) {
        if (LOG.isInfoEnabled()) {
            LOG.info("{} {} for player {} on level {} via `{}`",
                    cyan(LOG_PREFIX), bold("Spinning up headless Phaser"), userId, levelId, command);
        }
    }
}
