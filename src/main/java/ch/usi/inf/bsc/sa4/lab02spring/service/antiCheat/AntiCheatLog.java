package ch.usi.inf.bsc.sa4.lab02spring.service.antiCheat;

import static ch.usi.inf.bsc.sa4.lab02spring.utils.AnsiLogHelper.bold;
import static ch.usi.inf.bsc.sa4.lab02spring.utils.AnsiLogHelper.cyan;
import static ch.usi.inf.bsc.sa4.lab02spring.utils.AnsiLogHelper.green;
import static ch.usi.inf.bsc.sa4.lab02spring.utils.AnsiLogHelper.red;
import static ch.usi.inf.bsc.sa4.lab02spring.utils.AnsiLogHelper.yellow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AntiCheatLog {

    private static final Logger log = LoggerFactory.getLogger(AntiCheatLog.class);

    private AntiCheatLog() {
    }

    public static void levelStarted(final String userId, final String levelId) {
        log.info("{} {} {} started level {}",
                cyan("[ANTICHEAT]"), bold("Player"), userId, levelId);
    }

    public static void levelEntered(final String userId, final String levelId) {
        log.info("{} Player {} opened level {} for play",
                cyan("[ANTICHEAT]"), userId, levelId);
    }

    public static void levelCompleted(final String userId, final String levelId, final int frames) {
        log.info("{} {} {} completed level {} ({} frames), starting replay...",
                cyan("[ANTICHEAT]"), bold("Player"), userId, levelId, frames);
    }

    public static void replayValid(final String userId, final String levelId, final String reason) {
        log.info("{} {} for player {} on level {}: {}",
                green("[ANTICHEAT]"), bold("REPLAY VALID"), userId, levelId, reason);
    }

    public static void replayInvalid(final String userId, final String levelId, final String reason) {
        log.warn("{} {} for player {} on level {}: {}",
                red("[ANTICHEAT]"), bold("REPLAY INVALID"), userId, levelId, reason);
    }

    public static void replayError(final String userId, final String levelId, final String error) {
        log.error("{} Replay FAILED for player {} on level {}: {}",
                red("[ANTICHEAT]"), userId, levelId, error);
    }

    public static void replayTimeout(final String userId, final String levelId) {
        log.warn("{} Replay TIMED OUT for player {} on level {}",
                yellow("[ANTICHEAT]"), userId, levelId);
    }

    public static void replaySpinningUp(final String userId, final String levelId, final String command) {
        log.info("{} {} for player {} on level {} via `{}`",
                cyan("[ANTICHEAT]"), bold("Spinning up headless Phaser"), userId, levelId, command);
    }
}
