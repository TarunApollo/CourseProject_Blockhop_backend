package ch.usi.inf.bsc.sa4.lab02spring.service.antiCheat;

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

@Service
public class ReplayService {

    private static final long REPLAY_TIMEOUT_SECONDS = 30;

    private final ObjectMapper objectMapper;

    @Nullable
    private String npxPath;

    @Nullable
    private String replayScriptPath;

    @Nullable
    private Path frontendDirectory;

    public ReplayService(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ReplayResultDTO replay(final String userId,
                                   final String levelId,
                                   final String levelJson,
                                   final String inputLogJson) {
        final String npx = resolveNpx();
        final String script = resolveReplayScript();

        if (npx == null) {
            AntiCheatLog.replayError(userId, levelId, "npx not found on PATH");
            return new ReplayResultDTO(false, "error:npx_not_found", 0);
        }

        if (script == null) {
            AntiCheatLog.replayError(userId, levelId, "Replay script not found");
            return new ReplayResultDTO(false, "error:script_not_found", 0);
        }

        Path levelFile = null;
        Path inputFile = null;

        try {
            levelFile = Files.createTempFile("replay-level-", ".json");
            Files.writeString(levelFile, levelJson, StandardCharsets.UTF_8);

            inputFile = Files.createTempFile("replay-input-", ".json");
            Files.writeString(inputFile, inputLogJson, StandardCharsets.UTF_8);

            final String commandString = String.join(" ", npx, "tsx", script, levelFile.toString(), inputFile.toString());
            AntiCheatLog.replaySpinningUp(userId, levelId, commandString);

            final ProcessBuilder pb = new ProcessBuilder(
                    npx, "tsx", script, levelFile.toString(), inputFile.toString()
            );
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
                AntiCheatLog.replayTimeout(userId, levelId);
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
            AntiCheatLog.replayError(userId, levelId, String.valueOf(e.getMessage()));
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

    private record ReplayProcessResult(boolean valid, String reason, int frame) {
    }
}
