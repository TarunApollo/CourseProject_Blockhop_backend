package ch.usi.inf.bsc.sa4.lab02spring.service.anticheat;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.ReplayResultDTO;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

/// Tests for [ReplayService].
@SpringBootTest
@DisplayName("The Replay Service")
@SuppressWarnings("NullAway")
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

    /// Service field name for npx path.
    private static final String NPX_PATH_FIELD = "npxPath";

    /// Service field name for replay script path.
    private static final String REPLAY_SCRIPT_PATH_FIELD = "replayScriptPath";

    /// Service field name for frontend directory.
    private static final String FRONTEND_DIRECTORY_FIELD = "frontendDirectory";

    /// Service being tested.
    @Autowired
    private ReplayService replayService;

    /// Temporary folder used to host fake replay executables.
    @TempDir
    private Path tempDir;

    /// Clears cached paths before each test.
    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(replayService, NPX_PATH_FIELD, null);
        ReflectionTestUtils.setField(replayService, REPLAY_SCRIPT_PATH_FIELD, null);
        ReflectionTestUtils.setField(replayService, FRONTEND_DIRECTORY_FIELD, null);
    }

    /// Tests for missing replay setup.
    @Nested
    @DisplayName("when prerequisites are missing")
    class MissingPrerequisites {

        /// Checks that missing npx stops replay.
        @Test
        @DisplayName("returns npx_not_found when npx cannot be resolved")
        void returnsNpxNotFound() {
            ReflectionTestUtils.setField(replayService, NPX_PATH_FIELD, "");
            ReflectionTestUtils.setField(replayService, REPLAY_SCRIPT_PATH_FIELD, FAKE_SCRIPT_PATH);

            final ReplayResultDTO result = replayService.replay(REPLAY_REQUEST);

            Assertions.assertFalse(result.valid());
            Assertions.assertEquals("error:npx_not_found", result.reason());
            Assertions.assertEquals(0, result.frames());
        }

        /// Checks that a missing script stops replay.
        @Test
        @DisplayName("returns script_not_found when the replay script cannot be resolved")
        void returnsScriptNotFound() {
            ReflectionTestUtils.setField(replayService, NPX_PATH_FIELD, FAKE_NPX_PATH);
            ReflectionTestUtils.setField(replayService, REPLAY_SCRIPT_PATH_FIELD, "");

            final ReplayResultDTO result = replayService.replay(REPLAY_REQUEST);

            Assertions.assertFalse(result.valid());
            Assertions.assertEquals("error:script_not_found", result.reason());
            Assertions.assertEquals(0, result.frames());
        }
    }

    /// Tests for replay process execution.
    @Nested
    @DisplayName("when executing the replay process")
    class ProcessExecution {

        /// Checks that the final output line is parsed as the replay result.
        @Test
        @DisplayName("returns the replay JSON from the final process output line")
        void returnsReplayJsonFromFinalProcessOutputLine() throws IOException {
            final Path fakeNpx = writeExecutableNpx("""
                    #!/bin/sh
                    printf '%s\\n' 'booting replay'
                    printf '%s\\n' '{"valid":true,"reason":"level_complete","frame":123}'
                    """);
            ReflectionTestUtils.setField(replayService, NPX_PATH_FIELD, fakeNpx.toString());
            ReflectionTestUtils.setField(replayService, REPLAY_SCRIPT_PATH_FIELD,
                    tempDir.resolve("replay.ts").toString());
            ReflectionTestUtils.setField(replayService, FRONTEND_DIRECTORY_FIELD, tempDir);

            final ReplayResultDTO result = replayService.replay(REPLAY_REQUEST);

            Assertions.assertTrue(result.valid());
            Assertions.assertEquals("level_complete", result.reason());
            Assertions.assertEquals(123, result.frames());
        }

        /// Checks that process startup errors become replay execution errors.
        @Test
        @DisplayName("returns execution_error when the process cannot start")
        void returnsExecutionErrorWhenProcessCannotStart() {
            ReflectionTestUtils.setField(replayService, NPX_PATH_FIELD,
                    tempDir.resolve("missing-npx").toString());
            ReflectionTestUtils.setField(replayService, REPLAY_SCRIPT_PATH_FIELD,
                    tempDir.resolve("replay.ts").toString());
            ReflectionTestUtils.setField(replayService, FRONTEND_DIRECTORY_FIELD, tempDir);

            final ReplayResultDTO result = replayService.replay(REPLAY_REQUEST);

            Assertions.assertFalse(result.valid());
            Assertions.assertEquals(ReplayService.EXECUTION_ERROR, result.reason());
            Assertions.assertEquals(0, result.frames());
        }
    }

    /// Writes a fake executable `npx` script for process execution tests.
    private Path writeExecutableNpx(final String script) throws IOException {
        final Path fakeNpx = tempDir.resolve("npx");
        Files.writeString(fakeNpx, script);
        Assertions.assertTrue(fakeNpx.toFile().setExecutable(true));
        return fakeNpx;
    }
}
