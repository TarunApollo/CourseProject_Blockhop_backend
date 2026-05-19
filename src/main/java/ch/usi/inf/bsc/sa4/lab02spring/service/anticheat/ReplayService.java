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
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/// Service that executes a frontend replay script to verify submitted attempts.
@Service
public class ReplayService {

    /// Maximum time allowed for a replay process before it is treated as failed.
    private static final long REPLAY_TIMEOUT_SECONDS = 30;

    /// Environment variable name for the frontend directory path.
    private static final String FRONTEND_DIR_ENV = "FRONTEND_DIR";

    /// Mapper used to parse the replay process result.
    private final ObjectMapper objectMapper;

    /// Spring environment for resolving configuration properties.
    private final Environment environment;

    /// Cached path to the `npx` executable; an empty value means unresolved.
    @Nullable
    private String npxPath;

    /// Cached absolute path to the replay script; an empty value means unresolved.
    @Nullable
    private String replayScriptPath;

    /// Cached frontend project directory used as the replay process
    /// working directory.
    @Nullable
    private Path frontendDirectory;

    /// Constructs the replay service with the JSON mapper used for replay output.
    ///
    /// @param objectMapper mapper used to deserialize replay process output
    /// @param environment Spring environment for configuration resolution
    public ReplayService(final ObjectMapper objectMapper, final Environment environment) {
        this.objectMapper = objectMapper;
        this.environment = environment;
    }

    /// Runs the replay script for a level and input log and returns the
    /// verification result.
    ///
    /// @param replayRequest wrapper object for the request
    ///
    /// @return replay validity, reason, and final frame reported by the script
    public ReplayResultDTO replay(final ReplayRequest replayRequest) {
        final String npx = resolveNpx();
        final String script = resolveReplayScript();
        ReplayResultDTO result = validateReplayPrerequisites(replayRequest, npx, script);

        if (result == null) {
            Path levelFile = null;
            Path inputFile = null;

            try {
                levelFile = Files.createTempFile("replay-level-", ".json");
                Files.writeString(levelFile, replayRequest.levelJson(), StandardCharsets.UTF_8);

                inputFile = Files.createTempFile("replay-input-", ".json");
                Files.writeString(inputFile, replayRequest.inputLogJson(), StandardCharsets.UTF_8);

                result = executeReplayProcess(replayRequest, Objects.requireNonNull(npx),
                        Objects.requireNonNull(script), levelFile, inputFile);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                AntiCheatLog.replayError(replayRequest.userId(), replayRequest.levelId(), String.valueOf(e.getMessage()));
                result = new ReplayResultDTO(false, "error:execution_error", 0);
            } catch (final java.io.IOException e) {
                AntiCheatLog.replayError(replayRequest.userId(), replayRequest.levelId(), String.valueOf(e.getMessage()));
                result = new ReplayResultDTO(false, "error:execution_error", 0);
            } finally {
                deleteTemporaryFile(levelFile);
                deleteTemporaryFile(inputFile);
            }
        }

        return result;
    }

    @Nullable
    private ReplayResultDTO validateReplayPrerequisites(final ReplayRequest replayRequest, @Nullable final String npx,
            @Nullable final String script) {
        ReplayResultDTO result = null;

        if (npx == null) {
            AntiCheatLog.replayError(replayRequest.userId(), replayRequest.levelId(), "npx not found on PATH");
            result = new ReplayResultDTO(false, "error:npx_not_found", 0);
        } else if (script == null) {
            AntiCheatLog.replayError(replayRequest.userId(), replayRequest.levelId(), "Replay script not found");
            result = new ReplayResultDTO(false, "error:script_not_found", 0);
        }

        return result;
    }

    private ReplayResultDTO executeReplayProcess(final ReplayRequest replayRequest, final String npx, final String script,
            final Path levelFile, final Path inputFile) throws java.io.IOException, InterruptedException {
        final String commandString = String.join(" ", npx, "tsx", script, levelFile.toString(), inputFile.toString());
        AntiCheatLog.replaySpinningUp(replayRequest.userId(), replayRequest.levelId(), commandString);

        final ProcessBuilder processBuilder = new ProcessBuilder(
                npx, "tsx", script, levelFile.toString(), inputFile.toString());
        processBuilder.directory(resolveFrontendDirectory().toFile());
        processBuilder.redirectErrorStream(true);

        final Process process = processBuilder.start();
        ReplayResultDTO result;

        if (!process.waitFor(REPLAY_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            AntiCheatLog.replayTimeout(replayRequest.userId(), replayRequest.levelId());
            result = new ReplayResultDTO(false, "error:process_timeout", 0);
        } else {
            final String output = readProcessOutput(process);
            final ReplayProcessResult parsed = objectMapper.readValue(lastOutputLine(output), ReplayProcessResult.class);
            result = new ReplayResultDTO(parsed.valid(), parsed.reason(), parsed.frame());
        }

        return result;
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
                // Temporary replay files are best-effort cleanup only.
            }
        }
    }

    /// Resolves and caches the `npx` executable path from the local environment.
    ///
    /// @return the executable path, or null when `npx` is unavailable
    @Nullable
    private synchronized String resolveNpx() {
        String resolved = null;

        if (npxPath == null) {
            npxPath = resolveNpxPathValue();
        }

        if (!npxPath.isEmpty()) {
            resolved = npxPath;
        }

        return resolved;
    }

    private String resolveNpxPathValue() {
        String resolvedPath = "";

        try {
            final Process process = new ProcessBuilder("which", "npx").start();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                final String path = reader.readLine();
                process.waitFor(5, TimeUnit.SECONDS);
                if (path != null && !path.isBlank()) {
                    resolvedPath = path.trim();
                }
            }
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (final java.io.IOException ignored) {
            resolvedPath = "";
        }

        return resolvedPath;
    }

    /// Resolves and caches the TypeScript replay script path.
    ///
    /// @return the replay script path, or null when the script cannot be found
    @Nullable
    private synchronized String resolveReplayScript() {
        String resolved = null;

        if (replayScriptPath == null) {
            final Path candidate = resolveFrontendDirectory().resolve("replay/replay.ts");
            replayScriptPath = Files.exists(candidate) ? candidate.toAbsolutePath().normalize().toString() : "";
        }

        if (!replayScriptPath.isEmpty()) {
            resolved = replayScriptPath;
        }

        return resolved;
    }

    /// Resolves and caches the frontend directory used to run the replay script.
    ///
    /// @return the best available frontend directory path
    private synchronized Path resolveFrontendDirectory() {
        Path resolved = frontendDirectory;

        if (resolved == null) {
            final String envDir = environment.getProperty(FRONTEND_DIR_ENV);
            if (envDir != null && !envDir.isBlank()) {
                resolved = Path.of(envDir).toAbsolutePath().normalize();
            } else {
                resolved = Path.of("../frontend").toAbsolutePath().normalize();
            }

            frontendDirectory = resolved;
        }

        return resolved;
    }

    /// Raw JSON payload emitted by the replay script.
    ///
    /// @param valid  whether the replay completed as expected
    /// @param reason replay completion or error reason
    /// @param frame  frame count reported by the replay script
    private record ReplayProcessResult(boolean valid, String reason, int frame) {
    }
}
