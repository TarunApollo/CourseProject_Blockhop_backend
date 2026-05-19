package ch.usi.inf.bsc.sa4.lab02spring.service.anticheat;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.ReplayResultDTO;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

/// Service that executes a frontend replay script to verify submitted attempts.
@Service
public class ReplayService {

    /// Maximum time allowed for a replay process before it is treated as failed.
    private static final long REPLAY_TIMEOUT_SECONDS = 30;

    /// Mapper used to parse the replay process result.
    private final ObjectMapper objectMapper;

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
    public ReplayService(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
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

        if (npx == null) {
            AntiCheatLog.replayError(replayRequest.userId(), replayRequest.levelId(), "npx not found on PATH");
            return new ReplayResultDTO(false, "error:npx_not_found", 0);
        }

        if (script == null) {
            AntiCheatLog.replayError(replayRequest.userId(), replayRequest.levelId(), "Replay script not found");
            return new ReplayResultDTO(false, "error:script_not_found", 0);
        }

        Path levelFile = null;
        Path inputFile = null;

        try {
            levelFile = Files.createTempFile("replay-level-", ".json");
            Files.writeString(levelFile, replayRequest.levelJson(), StandardCharsets.UTF_8);

            inputFile = Files.createTempFile("replay-input-", ".json");
            Files.writeString(inputFile, replayRequest.inputLogJson(), StandardCharsets.UTF_8);

            final String commandString = String.join(" ", npx, "tsx", script, levelFile.toString(),
                    inputFile.toString());
            AntiCheatLog.replaySpinningUp(replayRequest.userId(), replayRequest.levelId(), commandString);

            final ProcessBuilder pb = new ProcessBuilder(
                    npx, "tsx", script, levelFile.toString(), inputFile.toString());
            pb.directory(resolveFrontendDirectory().toFile());
            pb.redirectErrorStream(true);

            final Process process = pb.start();
            final StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line);
                }
            }

            if (!process.waitFor(REPLAY_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                AntiCheatLog.replayTimeout(replayRequest.userId(), replayRequest.levelId());
                return new ReplayResultDTO(false, "error:process_timeout", 0);
            }

            final String resultLine = output.toString().trim();
            final int lastNewline = resultLine.lastIndexOf('\n');
            final String lastLine = lastNewline >= 0
                    ? resultLine.substring(lastNewline + 1).trim()
                    : resultLine;

            final ReplayProcessResult parsed = objectMapper.readValue(lastLine, ReplayProcessResult.class);

            return new ReplayResultDTO(parsed.valid(), parsed.reason(), parsed.frame());

        } catch (final Exception e) {
            AntiCheatLog.replayError(replayRequest.userId(), replayRequest.levelId(), String.valueOf(e.getMessage()));
            return new ReplayResultDTO(false, "error:execution_error", 0);
        } finally {
            try {
                if (levelFile != null) {
                    Files.deleteIfExists(levelFile);
                }
                if (inputFile != null) {
                    Files.deleteIfExists(inputFile);
                }
            } catch (final Exception ignored) {
            }
        }
    }

    /// Resolves and caches the `npx` executable path from the local environment.
    ///
    /// @return the executable path, or null when `npx` is unavailable
    @Nullable
    private String resolveNpx() {
        if (npxPath != null) {
            return npxPath.isEmpty() ? null : npxPath;
        }
        try {
            final Process np = new ProcessBuilder("which", "npx").start();
            final BufferedReader r = new BufferedReader(
                    new InputStreamReader(np.getInputStream()));
            final String path = r.readLine();
            np.waitFor(5, TimeUnit.SECONDS);
            npxPath = (path != null && !path.isBlank()) ? path.trim() : "";
        } catch (final Exception e) {
            npxPath = "";
        }
        return npxPath.isEmpty() ? null : npxPath;
    }

    /// Resolves and caches the TypeScript replay script path.
    ///
    /// @return the replay script path, or null when the script cannot be found
    @Nullable
    private String resolveReplayScript() {
        if (replayScriptPath != null) {
            return replayScriptPath.isEmpty() ? null : replayScriptPath;
        }
        final Path candidate = resolveFrontendDirectory().resolve("replay/replay.ts");
        if (Files.exists(candidate)) {
            replayScriptPath = candidate.toAbsolutePath().normalize().toString();
            return replayScriptPath;
        }
        replayScriptPath = "";
        return null;
    }

    /// Resolves and caches the frontend directory used to run the replay script.
    ///
    /// @return the best available frontend directory path
    private Path resolveFrontendDirectory() {
        if (frontendDirectory != null) {
            return frontendDirectory;
        }
        final Path[] candidates = {
                Path.of("../frontend"),
                Path.of("../clone/frontend"),
                Path.of("frontend")
        };
        for (final Path candidate : candidates) {
            if (Files.isDirectory(candidate) && Files.exists(candidate.resolve("package.json"))) {
                frontendDirectory = candidate.toAbsolutePath().normalize();
                return frontendDirectory;
            }
        }
        frontendDirectory = Path.of("../frontend").toAbsolutePath().normalize();
        return frontendDirectory;
    }

    /// Raw JSON payload emitted by the replay script.
    ///
    /// @param valid  whether the replay completed as expected
    /// @param reason replay completion or error reason
    /// @param frame  frame count reported by the replay script
    private record ReplayProcessResult(boolean valid, String reason, int frame) {
    }
}
