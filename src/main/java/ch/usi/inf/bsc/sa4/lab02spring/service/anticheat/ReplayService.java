package ch.usi.inf.bsc.sa4.lab02spring.service.anticheat;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.ReplayResultDTO;
import org.jspecify.annotations.Nullable;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/// Runs the replay script to check submitted attempts.
///
/// Suppressions:
/// - `pmd:DoNotUseThreads`: Project uses virtual threads for replay execution.
/// - `pmd:AvoidSynchronizedAtMethodLevel`: Safe lazy initialization
///   for cached paths.
@SuppressWarnings({ "pmd:DoNotUseThreads", "pmd:AvoidSynchronizedAtMethodLevel" })
@Service
public class ReplayService {

    /// Max seconds before replay fails.
    private static final long REPLAY_TIMEOUT_SECONDS = 30;

    /// Env var for overriding the replay script path.
    private static final String REPLAY_SCRIPT_PATH_ENV = "REPLAY_SCRIPT_PATH";

    /// Default bundled replay script path.
    private static final Path DEFAULT_REPLAY_SCRIPT_PATH = Path.of("anticheat/replay.bundle.mjs");

    /// Replay execution error reason.
    public static final String EXECUTION_ERROR = "error:execution_error";

    /// Reads replay JSON output.
    private final ObjectMapper objectMapper;

    /// Reads app settings.
    private final Environment environment;

    /// Cached `node` path. Empty means not found.
    @Nullable
    private String nodePath;

    /// Cached replay script path. Empty means not found.
    @Nullable
    private String replayScriptPath;

    /// Creates the replay service.
    ///
    /// @param objectMapper reads replay JSON output
    /// @param environment reads app settings
    public ReplayService(final ObjectMapper objectMapper, final Environment environment) {
        this.objectMapper = objectMapper;
        this.environment = environment;
    }

    /// Runs replay for one attempt.
    ///
    /// @param replayRequest replay input data
    ///
    /// @return whether replay passed, why, and the final frame
    public ReplayResultDTO replay(final ReplayRequest replayRequest) {
        final String script = resolveReplayScript();
        final String node = resolveNode();
        ReplayResultDTO result = validateReplayPrerequisites(replayRequest, script, node);

        if (result == null) {
            Path levelFile = null;
            Path inputFile = null;

            try {
                levelFile = Files.createTempFile("replay-level-", ".json");
                Files.writeString(levelFile, replayRequest.levelJson(), StandardCharsets.UTF_8);

                inputFile = Files.createTempFile("replay-input-", ".json");
                Files.writeString(inputFile, replayRequest.inputLogJson(), StandardCharsets.UTF_8);

                result = executeReplayProcess(replayRequest, Objects.requireNonNull(node),
                        Objects.requireNonNull(script), levelFile, inputFile);
            } catch (final InterruptedException e) {
                // Reset the interrupted flag so the system knows this operation was cancelled.
                // Without this, the interruption is swallowed and
                // the app might hang during shutdown.
                Thread.currentThread().interrupt();
                AntiCheatLog.replayError(replayRequest.userId(), replayRequest.levelId(), String.valueOf(e.getMessage()));
                result = new ReplayResultDTO(false, EXECUTION_ERROR, 0);
            } catch (final tools.jackson.core.JacksonException e) {
                AntiCheatLog.replayError(replayRequest.userId(), replayRequest.levelId(), String.valueOf(e.getMessage()));
                result = new ReplayResultDTO(false, EXECUTION_ERROR, 0);
            } catch (final java.io.IOException e) {
                AntiCheatLog.replayError(replayRequest.userId(), replayRequest.levelId(), String.valueOf(e.getMessage()));
                result = new ReplayResultDTO(false, EXECUTION_ERROR, 0);
            } finally {
                deleteTemporaryFile(levelFile);
                deleteTemporaryFile(inputFile);
            }
        }

        return result;
    }

    @Nullable
    private ReplayResultDTO validateReplayPrerequisites(final ReplayRequest replayRequest,
            @Nullable final String script,
            @Nullable final String node) {
        ReplayResultDTO result = null;

        if (script == null) {
            AntiCheatLog.replayError(replayRequest.userId(), replayRequest.levelId(), "Replay script not found");
            result = new ReplayResultDTO(false, "error:script_not_found", 0);
        } else if (node == null) {
            AntiCheatLog.replayError(replayRequest.userId(), replayRequest.levelId(), "node not found on PATH");
            result = new ReplayResultDTO(false, "error:node_not_found", 0);
        }

        return result;
    }

    private ReplayResultDTO executeReplayProcess(final ReplayRequest replayRequest,
            final String node,
            final String script,
            final Path levelFile, final Path inputFile) throws java.io.IOException, InterruptedException {
        final List<String> command = List.of(node, script, levelFile.toString(), inputFile.toString());
        final String commandString = String.join(" ", command);
        AntiCheatLog.replaySpinningUp(replayRequest.userId(), replayRequest.levelId(), commandString);

        final ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.directory(resolveReplayWorkingDirectory(script).toFile());
        processBuilder.redirectErrorStream(true);

        final Process process = processBuilder.start();
        final ReplayResultDTO result;

        // Block the virtual thread until the external replay script finishes or times out.
        // Virtual threads are cheap to park, so blocking here is safe and efficient.
        if (process.waitFor(REPLAY_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            final String output = readProcessOutput(process);
            final ReplayProcessResult parsed = objectMapper.readValue(lastOutputLine(output), ReplayProcessResult.class);
            result = new ReplayResultDTO(parsed.valid(), parsed.reason(), parsed.frame());
        } else {
            process.destroyForcibly();
            AntiCheatLog.replayTimeout(replayRequest.userId(), replayRequest.levelId());
            result = new ReplayResultDTO(false, "error:process_timeout", 0);
        }

        return result;
    }

    private Path resolveReplayWorkingDirectory(final String script) {
        final Path scriptPath = Path.of(script).toAbsolutePath().normalize();
        final Path parent = scriptPath.getParent();
        return parent == null ? Path.of(".").toAbsolutePath().normalize() : parent;
    }

    private String readProcessOutput(final Process process) throws java.io.IOException {
        final StringBuilder output = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line = reader.readLine();

            while (line != null) {
                if (output.length() > 0) {
                    output.append('\n');
                }
                output.append(line);
                line = reader.readLine();
            }
        }

        return output.toString();
    }

    private String lastOutputLine(final String output) {
        final String trimmedOutput = output.trim();
        final int lastNewline = trimmedOutput.lastIndexOf('\n');
        String lastLine = trimmedOutput;

        if (lastNewline >= 0) {
            lastLine = trimmedOutput.substring(lastNewline + 1).trim();
        }

        return lastLine;
    }

    private void deleteTemporaryFile(@Nullable final Path file) {
        if (file != null) {
            try {
                Files.deleteIfExists(file);
            } catch (final java.io.IOException ignored) {
                // Try cleanup, but do not fail replay for it.
            }
        }
    }

    @Nullable
    private synchronized String resolveNode() {
        String resolved = null;

        if (nodePath == null) {
            nodePath = resolveBinaryFromPath("node");
        }

        if (!nodePath.isEmpty()) {
            resolved = nodePath;
        }

        return resolved;
    }

    private String resolveBinaryFromPath(final String binaryName) {
        String resolvedPath = "";

        try {
            final Process process = new ProcessBuilder("which", binaryName).start();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                final String path = reader.readLine();
                process.waitFor(5, TimeUnit.SECONDS);
                if (path != null && !path.isBlank()) {
                    resolvedPath = path.trim();
                }
            }
        } catch (final InterruptedException e) {
            // Reset the interrupted flag so the system knows this operation was cancelled.
            Thread.currentThread().interrupt();
        } catch (final java.io.IOException ignored) {
            resolvedPath = "";
        }

        return resolvedPath;
    }

    /// Finds and remembers the replay script.
    ///
    /// @return script path, or null if it is missing
    @Nullable
    private synchronized String resolveReplayScript() {
        String resolved = null;

        if (replayScriptPath == null) {
            final String configuredReplayScriptPath = configuredPath(REPLAY_SCRIPT_PATH_ENV);
            final Path candidate = configuredReplayScriptPath == null
                    ? DEFAULT_REPLAY_SCRIPT_PATH
                    : Path.of(configuredReplayScriptPath).toAbsolutePath().normalize();
            replayScriptPath = Files.exists(candidate) ? candidate.toAbsolutePath().normalize().toString() : "";
        }

        if (!replayScriptPath.isEmpty()) {
            resolved = replayScriptPath;
        }

        return resolved;
    }

    @Nullable
    private String configuredPath(final String propertyName) {
        final String configured = environment.getProperty(propertyName);
        return configured == null || configured.isBlank() ? null : configured;
    }

    /// JSON line printed by the replay script.
    ///
    /// @param valid true if replay passed
    /// @param reason replay result reason
    /// @param frame frame count from replay
    private record ReplayProcessResult(boolean valid, String reason, int frame) {
    }
}
