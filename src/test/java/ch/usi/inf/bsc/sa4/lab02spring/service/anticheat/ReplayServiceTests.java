package ch.usi.inf.bsc.sa4.lab02spring.service.anticheat;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.ReplayResultDTO;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

/// Tests for [ReplayService].
@SpringBootTest
@DisplayName("The Replay Service")
class ReplayServiceTests {

    /// Replay request used by these tests.
    private static final ReplayRequest REPLAY_REQUEST = new ReplayRequest(
            "user-1",
            "level-1",
            "{\"layers\":[]}",
            "[]");

    /// Fake npx path.
    private static final String FAKE_NPX_PATH = "/tmp/fake-npx";

    /// Fake replay script path.
    private static final String FAKE_SCRIPT_PATH = "/tmp/replay.ts";

    /// Service being tested.
    @Autowired
    private ReplayService replayService;

    /// Clears cached paths before each test.
    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(replayService, "npxPath", null);
        ReflectionTestUtils.setField(replayService, "replayScriptPath", null);
        ReflectionTestUtils.setField(replayService, "frontendDirectory", null);
    }

    /// Tests for missing replay setup.
    @Nested
    @DisplayName("when prerequisites are missing")
    class MissingPrerequisites {

        /// Checks that missing npx stops replay.
        @Test
        @DisplayName("returns npx_not_found when npx cannot be resolved")
        void returnsNpxNotFound() {
            ReflectionTestUtils.setField(replayService, "npxPath", "");
            ReflectionTestUtils.setField(replayService, "replayScriptPath", FAKE_SCRIPT_PATH);

            final ReplayResultDTO result = replayService.replay(REPLAY_REQUEST);

            Assertions.assertFalse(result.valid());
            Assertions.assertEquals("error:npx_not_found", result.reason());
            Assertions.assertEquals(0, result.frames());
        }

        /// Checks that a missing script stops replay.
        @Test
        @DisplayName("returns script_not_found when the replay script cannot be resolved")
        void returnsScriptNotFound() {
            ReflectionTestUtils.setField(replayService, "npxPath", FAKE_NPX_PATH);
            ReflectionTestUtils.setField(replayService, "replayScriptPath", "");

            final ReplayResultDTO result = replayService.replay(REPLAY_REQUEST);

            Assertions.assertFalse(result.valid());
            Assertions.assertEquals("error:script_not_found", result.reason());
            Assertions.assertEquals(0, result.frames());
        }
    }
}
