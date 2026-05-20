package ch.usi.inf.bsc.sa4.lab02spring.service.anticheat;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.jspecify.annotations.Nullable;
import org.slf4j.LoggerFactory;

/// Tests for [AntiCheatLog].
@DisplayName("The Anti Cheat Log")
@SuppressWarnings("fb-contrib:LO_SUSPECT_LOG_CLASS")
class AntiCheatLogTests {

    /// User id used in log tests.
    private static final String USER_ID = "user-1";

    /// Level id used in log tests.
    private static final String LEVEL_ID = "level-1";

    /// Logger used by [AntiCheatLog].
    private final Logger logger = (Logger) LoggerFactory.getLogger(AntiCheatLog.class);

    /// In-memory appender used to inspect emitted log events.
    private final ListAppender<ILoggingEvent> appender = new ListAppender<>();

    /// Original logger level restored after each test.
    @Nullable
    private Level originalLevel;

    /// Attaches an in-memory appender before each test.
    @BeforeEach
    void setUp() {
        originalLevel = logger.getLevel();
        logger.setLevel(Level.ALL);
        appender.start();
        logger.addAppender(appender);
    }

    /// Restores the logger after each test.
    @AfterEach
    void tearDown() {
        logger.detachAppender(appender);
        appender.stop();
        logger.setLevel(originalLevel);
        appender.list.clear();
    }

    /// Checks lifecycle log methods.
    @Test
    @DisplayName("writes lifecycle replay events")
    void writesLifecycleReplayEvents() {
        AntiCheatLog.levelStarted(USER_ID, LEVEL_ID);
        AntiCheatLog.levelEntered(USER_ID, LEVEL_ID);
        AntiCheatLog.levelCompleted(USER_ID, LEVEL_ID, 42);
        AntiCheatLog.replaySpinningUp(USER_ID, LEVEL_ID, "npx tsx replay.ts");

        final String logs = loggedMessages();
        Assertions.assertTrue(logs.contains("started level"));
        Assertions.assertTrue(logs.contains("opened level"));
        Assertions.assertTrue(logs.contains("completed level"));
        Assertions.assertTrue(logs.contains("Spinning up headless Phaser"));
    }

    /// Checks replay outcome log methods.
    @Test
    @DisplayName("writes replay outcome events")
    void writesReplayOutcomeEvents() {
        AntiCheatLog.replayValid(USER_ID, LEVEL_ID, "level_complete");
        AntiCheatLog.replayMismatch(USER_ID, LEVEL_ID, "frame mismatch");
        AntiCheatLog.replaySuspicious(USER_ID, LEVEL_ID, "fingerprint");
        AntiCheatLog.replayInvalid(USER_ID, LEVEL_ID, "error");
        AntiCheatLog.replayError(USER_ID, LEVEL_ID, "boom");
        AntiCheatLog.replayTimeout(USER_ID, LEVEL_ID);

        final String logs = loggedMessages();
        Assertions.assertTrue(logs.contains("REPLAY VERIFIED"));
        Assertions.assertTrue(logs.contains("REPLAY MISMATCH"));
        Assertions.assertTrue(logs.contains("REPLAY SUSPICIOUS"));
        Assertions.assertTrue(logs.contains("REPLAY INVALID"));
        Assertions.assertTrue(logs.contains("Replay FAILED"));
        Assertions.assertTrue(logs.contains("Replay TIMED OUT"));
    }

    /// Returns all captured formatted log messages.
    private String loggedMessages() {
        return appender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .toList()
                .toString();
    }
}
