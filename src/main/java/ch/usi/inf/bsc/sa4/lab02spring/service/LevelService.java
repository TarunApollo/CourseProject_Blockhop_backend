package ch.usi.inf.bsc.sa4.lab02spring.service;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.CreateLevelSingleTileDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.UpdateLevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.*;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.CloneLevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.CreateLevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import ch.usi.inf.bsc.sa4.lab02spring.repository.LevelRepository;
import ch.usi.inf.bsc.sa4.lab02spring.utils.LevelPublishedException;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;


@Service
public class LevelService {
    
    private final LevelRepository levelRepository;
    private final UserService userService;

    @Autowired
    public LevelService(LevelRepository levelRepository, UserService userService) {
        this.levelRepository = levelRepository;
        this.userService = userService;
    }

    public Level createLevel(CreateLevelDTO createLevelDTO, String userId) {
        Level level = new Level(createLevelDTO.title(), createLevelDTO.description(), userId);
        return levelRepository.save(level);
    }

    public Optional<Level> cloneLevel(CloneLevelDTO cloneLevelDTO, String userId) {
        Optional<Level> optLevel = this.levelRepository.findById(cloneLevelDTO.sourceLevelId());
        return optLevel.filter((level) -> level.getCreatorId().equals(userId))
                .map((level) -> this.levelRepository.save(level.cloneFor(userId)));
    }

    public List<Level> getAllLevels() {
        return levelRepository.findAll();
    }
    public Level updateLevelProperties(String userId ,String levelId, UpdateLevelDTO dto) {
        Level level = levelRepository.findById(levelId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        String creator = level.getCreatorId();
        if (!userId.equals(creator)){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        if(level.isPublished()){
            throw new LevelPublishedException("Level is already published!");
        }
        dto.title().ifPresent(level::setTitle);
        dto.description().ifPresent(level::setDescription);
        dto.clearCondition().ifPresent(level::setClearCondition);
        return levelRepository.save(level);
    }

    public Level createLevelSingleTile(CreateLevelSingleTileDTO dto){
        final String creatorId = "DEFAULT_TEST";
        Position pos = new Position(dto.posX(), dto.posY());
        Level level = new Level(dto.title(), dto.description(), creatorId);
        Map<Position, GameObject> map = level.getObjectLayer();
        GameObject gameObject = new Coin(0, pos, 100);
        Box boxObject = new Box(1, new Position(3,3), BoxContentType.GOLD);
        map.put(pos, gameObject);
        map.put(boxObject.pos(), boxObject);
        return this.levelRepository.save(level);
    }
}
