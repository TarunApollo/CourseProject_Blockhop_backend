package ch.usi.inf.bsc.sa4.lab02spring.service;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.CloneLevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.CreateLevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.usi.inf.bsc.sa4.lab02spring.repository.LevelRepository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class LevelService {
    
    private final LevelRepository levelRepository;

    @Autowired
    public LevelService(LevelRepository levelRepository, UserService userService) {
        this.levelRepository = levelRepository;
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
}
