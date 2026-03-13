package ch.usi.inf.bsc.sa4.lab02spring.service;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.*;
import ch.usi.inf.bsc.sa4.lab02spring.model.*;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.repository.AttemptRepository;
import ch.usi.inf.bsc.sa4.lab02spring.utils.*;
import ch.usi.inf.bsc.sa4.lab02spring.utils.levelTesting.UpdateLevelObjectDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.repository.LevelRepository;

import java.util.List;
import java.util.Optional;


@Service
public class LevelService {
    
    private final LevelRepository levelRepository;
    private final UserService userService;

    @Autowired
    public LevelService(LevelRepository levelRepository, UserService userService, AttemptRepository attemptRepository) {
        this.levelRepository = levelRepository;
        this.userService = userService;
    }

    public Level createLevel(CreateLevelDTO createLevelDTO, String userId) {
        User user = userService.getById(userId).orElseThrow(UserNotFoundException::new);
        Level level = new Level(createLevelDTO.title(), createLevelDTO.description(), user);
        return levelRepository.save(level);
    }

    public Optional<Level> cloneLevel(CloneLevelDTO cloneLevelDTO, User user) {
        Optional<Level> optLevel = this.levelRepository.findById(cloneLevelDTO.sourceLevelId());
        return optLevel.filter((level) -> level.getCreator().getId().equals(user.getId()))
                .map((level) -> this.levelRepository.save(level.cloneFor(user)));
    }

    public List<Level> getAllLevels() {
        return levelRepository.findAll();
    }
    public List<LevelDTO> getCreatedLevelsByUser(User creator) {
        return levelRepository.findByCreator(creator).stream()
                .map(LevelDTO::new)
                .toList();
    }

    public Level updateLevelProperties(User user, String levelId, UpdateLevelDTO dto) {
        Level level = levelRepository.findById(levelId).orElseThrow(LevelNotFoundException::new);
        if (!level.getCreator().getId().equals(user.getId())) {
            throw new ForbiddenUserException("Can't update a level that's not yours");
        }
        if (level.isPublished()) {
            throw new LevelPublishedException("Level is already published");
        }
        dto.title().ifPresent(level::setTitle);
        dto.description().ifPresent(level::setDescription);
        dto.clearCondition().ifPresent(level::setClearCondition);
        return levelRepository.save(level);
    }

    // Method for testing adding an object into the objectLayer of a given level
//    public Level updateLevelObject(User user, String levelId, UpdateLevelObjectDTO dto){
//        Level level = levelRepository.findById(levelId).orElseThrow(LevelNotFoundException::new);
//        if (!level.getCreator().getId().equals(user.getId())) {
//            throw new ForbiddenUserException("Can't update a level that's not yours");
//        }
//        if (level.isPublished()) {
//            throw new LevelPublishedException("Level is already published");
//        }
//        level.putObjectLayer(dto.pos(), new Coin(1, dto.pos(), 100));
//        return this.levelRepository.save(level);
//    }
}
