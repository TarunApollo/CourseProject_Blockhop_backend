package ch.usi.inf.bsc.sa4.lab02spring.service;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.UpdateLevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.ClearCondition;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.CloneLevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.CreateLevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.usi.inf.bsc.sa4.lab02spring.repository.LevelRepository;
import ch.usi.inf.bsc.sa4.lab02spring.utils.LevelPublishedException;

import java.util.List;
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
    public Optional<Level> updateLevelProperties(String userId ,String levelId, UpdateLevelDTO dto) {
        Optional<Level> optLevel = levelRepository.findById(levelId);
        return optLevel.map(level -> {
            if (!level.getCreatorId().equals(userId)) {
                throw new IllegalArgumentException("User is not the creator of this level.");
            }
            if (level.isPublished()) {
                throw new LevelPublishedException("Level is already published.");
            }
        dto.title().ifPresent(level::setTitle);
        dto.description().ifPresent(level::setDescription);
        dto.clearCondition().ifPresent(level::setClearCondition);
        return levelRepository.save(level);
        });
    }
}
