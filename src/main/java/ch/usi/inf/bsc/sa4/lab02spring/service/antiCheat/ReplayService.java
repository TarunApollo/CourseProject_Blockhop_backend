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
            return new ReplayResultDTO(false, "npx_not_found", 0);
        }

        if (script == null) {
            AntiCheatLog.replayError(userId, levelId, "Replay script not found");
            return new ReplayResultDTO(false, "script_not_found", 0);
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
            pb.directory(Path.of("../clone/frontend").toFile());
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
                return new ReplayResultDTO(false, "timeout", 0);
            }

            final String resultLine = output.toString().trim();
            final int lastNewline = resultLine.lastIndexOf('\n');
            final String lastLine = lastNewline >= 0
                    ? resultLine.substring(lastNewline + 1).trim()
                    : resultLine;

            final ReplayProcessResult parsed = objectMapper.readValue(lastLine, ReplayProcessResult.class);

            if (parsed.valid()) {
                AntiCheatLog.replayValid(userId, levelId, parsed.reason());
            } else {
                AntiCheatLog.replayInvalid(userId, levelId, parsed.reason());
            }

            return new ReplayResultDTO(parsed.valid(), parsed.reason(), parsed.frame());

        } catch (final Exception e) {
            AntiCheatLog.replayError(userId, levelId, String.valueOf(e.getMessage()));
            return new ReplayResultDTO(false, "execution_error", 0);
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
        final String[] candidates = {
                "../clone/frontend/replay/replay.ts",
                "replay/replay.ts"
        };
        for (final String candidate : candidates) {
            if (Files.exists(Path.of(candidate))) {
                replayScriptPath = Path.of(candidate).toAbsolutePath().normalize().toString();
                return replayScriptPath;
            }
        }
        replayScriptPath = "";
        return null;
    }

    private record ReplayProcessResult(boolean valid, String reason, int frame) {
    }
}
