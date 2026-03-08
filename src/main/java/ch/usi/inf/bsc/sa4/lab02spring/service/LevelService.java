package ch.usi.inf.bsc.sa4.lab02spring.service;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.UpdateLevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.ClearCondition;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.usi.inf.bsc.sa4.lab02spring.repository.LevelRepository;

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

    public Optional<Level> updateLevelProperties(String levelId, UpdateLevelDTO dto) {
        Optional<Level> optLevel = levelRepository.findById(levelId);
        return optLevel.map(level -> {
            if (level.isPublished()) {
                throw new RuntimeException("Level is already published");
            }
        dto.title().ifPresent(level::setTitle);
        dto.description().ifPresent(level::setDescription);
        dto.clearCondition().ifPresent(level::setClearCondition);
        return levelRepository.save(level);
        });
    }
}
