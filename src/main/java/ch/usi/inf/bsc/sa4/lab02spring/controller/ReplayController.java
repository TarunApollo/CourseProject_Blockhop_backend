package ch.usi.inf.bsc.sa4.lab02spring.controller;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.ReplayRequestDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.ReplayResultDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.TileSet;
import ch.usi.inf.bsc.sa4.lab02spring.repository.LevelRepository;
import ch.usi.inf.bsc.sa4.lab02spring.service.TileSetService;
import ch.usi.inf.bsc.sa4.lab02spring.service.antiCheat.AntiCheatLog;
import ch.usi.inf.bsc.sa4.lab02spring.service.antiCheat.ReplayService;
import ch.usi.inf.bsc.sa4.lab02spring.utils.AuthUtils;
import ch.usi.inf.bsc.sa4.lab02spring.utils.LevelNotFoundException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.converter.LayerToTiledMapConverter;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/replay")
public class ReplayController {

    private final ReplayService replayService;
    private final LevelRepository levelRepository;
    private final TileSetService tileSetService;
    private final ObjectMapper objectMapper;

    public ReplayController(final ReplayService replayService,
                            final LevelRepository levelRepository,
                            final TileSetService tileSetService,
                            final ObjectMapper objectMapper) {
        this.replayService = replayService;
        this.levelRepository = levelRepository;
        this.tileSetService = tileSetService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/start")
    public ResponseEntity<Void> startRun(
            final Authentication authentication,
            @RequestBody final StartRequest request) {

        final @Nullable String userId = AuthUtils.getUserIdFromAuth(authentication);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        final Level level = levelRepository.findById(request.levelId())
                .orElseThrow(LevelNotFoundException::new);

        level.ensurePlayable(userId);

        AntiCheatLog.levelStarted(userId, request.levelId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/submit")
    public ResponseEntity<ReplayResultDTO> submitRun(
            final Authentication authentication,
            @RequestBody final ReplayRequestDTO request) {

        final @Nullable String userId = AuthUtils.getUserIdFromAuth(authentication);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        final Level level = levelRepository.findById(request.levelId())
                .orElseThrow(LevelNotFoundException::new);

        level.ensurePlayable(userId);

        AntiCheatLog.levelCompleted(userId, request.levelId(), request.totalFrames());

        final TileSet tileSet = tileSetService.getTileSet();
        final Map<String, Object> tiledMap = LayerToTiledMapConverter.convertPipeline(
                level, tileSet, tileSetService);

        final String levelJson;
        final String inputJson;
        try {
            levelJson = objectMapper.writeValueAsString(tiledMap);
            inputJson = serializeInputLog(request);
        } catch (final Exception e) {
            AntiCheatLog.replayError(userId, request.levelId(), String.valueOf(e.getMessage()));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        final ReplayResultDTO result = replayService.replay(
                userId, request.levelId(), levelJson, inputJson);

        return ResponseEntity.ok(result);
    }

    private String serializeInputLog(final ReplayRequestDTO request) throws Exception {
        final List<SerializedReplayFrame> frames = request.inputLog().stream()
                .map(frame -> new SerializedReplayFrame(
                        frame.frame(),
                        new SerializedPlayerInput(frame.left(), frame.right(), frame.jump(), frame.run())))
                .toList();
        return objectMapper.writeValueAsString(frames);
    }

    public record StartRequest(String levelId) {
    }

    private record SerializedReplayFrame(int frame, SerializedPlayerInput input) {
    }

    private record SerializedPlayerInput(boolean left, boolean right, boolean jump, boolean run) {
    }
}
