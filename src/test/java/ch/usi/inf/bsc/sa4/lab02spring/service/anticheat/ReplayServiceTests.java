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

    /// Service field name for replay script path.
    private static final String REPLAY_SCRIPT_PATH_FIELD = "replayScriptPath";

    /// Service field name for node path.
    private static final String NODE_PATH_FIELD = "nodePath";

    /// Service being tested.
    @Autowired
    private ReplayService replayService;

    /// Temporary folder used to host fake replay executables.
    @TempDir
    private Path tempDir;

    /// Clears cached paths before each test.
    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(replayService, NODE_PATH_FIELD, null);
        ReflectionTestUtils.setField(replayService, REPLAY_SCRIPT_PATH_FIELD, null);
    }

    /// Tests for missing replay setup.
    @Nested
    @DisplayName("when prerequisites are missing")
    class MissingPrerequisites {

        /// Checks that missing node stops replay.
        @Test
        @DisplayName("returns node_not_found when node cannot be resolved")
        void returnsNodeNotFound() {
            final Path replayScript = tempDir.resolve("replay.bundle.mjs");
            writeFile(replayScript, "");
            ReflectionTestUtils.setField(replayService, NODE_PATH_FIELD, "");
            ReflectionTestUtils.setField(replayService, REPLAY_SCRIPT_PATH_FIELD,
                    replayScript.toString());

            final ReplayResultDTO result = replayService.replay(REPLAY_REQUEST);

            Assertions.assertFalse(result.valid());
            Assertions.assertEquals("error:node_not_found", result.reason());
            Assertions.assertEquals(0, result.frames());
        }

        /// Checks that a missing script stops replay.
        @Test
        @DisplayName("returns script_not_found when the replay script cannot be resolved")
        void returnsScriptNotFound() {
            ReflectionTestUtils.setField(replayService, NODE_PATH_FIELD, tempDir.resolve("node").toString());
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
        @DisplayName("uses node directly for the replay bundle")
        void usesNodeDirectlyForReplayBundle() throws IOException {
            final Path replayScript = tempDir.resolve("replay.bundle.mjs");
            writeFile(replayScript, "");
            final Path fakeNode = writeExecutable("node", """
                    #!/bin/sh
                    if [ "$1" != "%s" ]; then
                      printf 'wrong script path: %%s\n' "$1"
                      exit 1
                    fi
                    printf '%%s\\n' 'booting replay bundle'
                    printf '%%s\\n' '{"valid":true,"reason":"level_complete","frame":321}'
                    """.formatted(replayScript));
            ReflectionTestUtils.setField(replayService, NODE_PATH_FIELD, fakeNode.toString());
            ReflectionTestUtils.setField(replayService, REPLAY_SCRIPT_PATH_FIELD, replayScript.toString());

            final ReplayResultDTO result = replayService.replay(REPLAY_REQUEST);

            Assertions.assertTrue(result.valid());
            Assertions.assertEquals("level_complete", result.reason());
            Assertions.assertEquals(321, result.frames());
        }

        /// Checks that process startup errors become replay execution errors.
        @Test
        @DisplayName("returns execution_error when the process cannot start")
        void returnsExecutionErrorWhenProcessCannotStart() {
            final Path replayScript = tempDir.resolve("replay.bundle.mjs");
            writeFile(replayScript, "");
            ReflectionTestUtils.setField(replayService, NODE_PATH_FIELD,
                    tempDir.resolve("missing-node").toString());
            ReflectionTestUtils.setField(replayService, REPLAY_SCRIPT_PATH_FIELD,
                    replayScript.toString());

            final ReplayResultDTO result = replayService.replay(REPLAY_REQUEST);

            Assertions.assertFalse(result.valid());
            Assertions.assertEquals(ReplayService.EXECUTION_ERROR, result.reason());
            Assertions.assertEquals(0, result.frames());
        }
    }

    /// Writes a fake executable script for process execution tests.
    private Path writeExecutable(final String fileName, final String script) throws IOException {
        final Path executable = tempDir.resolve(fileName);
        writeFile(executable, script);
        Assertions.assertTrue(executable.toFile().setExecutable(true));
        return executable;
    }

    private void writeFile(final Path file, final String content) {
        try {
            Files.writeString(file, content);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}
