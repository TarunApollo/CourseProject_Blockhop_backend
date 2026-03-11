package ch.usi.inf.bsc.sa4.lab02spring.service;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.LevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.UpdateLevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.CloneLevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.CreateLevelDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import ch.usi.inf.bsc.sa4.lab02spring.repository.LevelRepository;
import ch.usi.inf.bsc.sa4.lab02spring.utils.LevelPublishedException;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
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
    /// Creates a level for the given user id
    /// @spec.requires createLevelDTO and userId are not null.
    /// @spec.effects saves a new Level to the repository with the given title,
    ///              description, and creatorId set to userId.
    /// @param createLevelDTO the DTO containing the title and description of the new level.
    /// @param userId the unique identifier of the user.
    /// @returns the newly created and saved Level.
    public Level createLevel(CreateLevelDTO createLevelDTO, String userId) {
        Level level = new Level(createLevelDTO.title(), createLevelDTO.description(), userId);
        return levelRepository.save(level);
    }

    /// Clones an existing level for the given user.
    /// @spec.requires cloneLevelDTO and userId are not null.
    /// @spec.effects if a level with the given sourceLevelId exists and belongs
    ///               to the given user, saves a clone of it to the repository
    ///               with the given userId as the new creatorId.
    /// @param cloneLevelDTO the DTO containing the id of the level to clone.
    /// @param userId the unique identifier of the user cloning the level.
    /// @returns a non-empty Optional containing the cloned Level if the source
    ///          level exists and belongs to the user, an empty Optional otherwise.
    public Optional<Level> cloneLevel(CloneLevelDTO cloneLevelDTO, String userId) {
        Optional<Level> optLevel = this.levelRepository.findById(cloneLevelDTO.sourceLevelId());
        return optLevel.filter((level) -> level.getCreatorId().equals(userId))
                .map((level) -> this.levelRepository.save(level.cloneFor(userId)));
    }

    /// @returns a list of all levels, or an empty list if none exist.
    public List<Level> getAllLevels() {
        return levelRepository.findAll();
    }

    /// Returns all levels created by the given user as DTOs.
    /// @spec.requires userId is not null.
    /// @param userId the unique identifier of the user.
    /// @returns a list of LevelDTOs for all levels created by the given user,
    ///          or an empty list if the user has not created any levels.
    public List<LevelDTO> getCreatedLevelsByUser(String userId) {
        return levelRepository.findByCreatorId(userId).stream()
                .map(LevelDTO::new)
                .toList();
    }

    /// Updates the properties of an existing level.
    /// @spec.requires userId, levelId, and dto are not null.
    /// @spec.modifies the level with the given levelId in the repository.
    /// @spec.effects updates the title, description, and/or clearCondition
    ///               of the level if present in the dto, then saves the updated level.
    /// @param userId the unique identifier of the user attempting the update.
    /// @param levelId the unique identifier of the level to update.
    /// @param dto the DTO containing the optional new title, description, and clearCondition.
    /// @returns the updated and saved Level.
    /// @throws ResponseStatusException with 404 NOT FOUND if no level exists with the given levelId.
    /// @throws ResponseStatusException with 401 UNAUTHORIZED if the user is not the creator of the level.
    /// @throws LevelPublishedException if the level is already published.
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
}
