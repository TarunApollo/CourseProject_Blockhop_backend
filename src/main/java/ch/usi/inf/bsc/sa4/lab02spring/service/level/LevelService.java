package ch.usi.inf.bsc.sa4.lab02spring.service.level;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.CloneLevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.CreateLevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.LevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.UpdateLevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.repository.LevelRepository;
import ch.usi.inf.bsc.sa4.lab02spring.service.UserService;
import ch.usi.inf.bsc.sa4.lab02spring.utils.LevelNotFoundException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class LevelService {
    private final LevelRepository levelRepository;
    private final UserService userService;

    /// Constructs a new LevelService with the given dependencies.
    /// @param levelRepository the repository for accessing level data
    /// @param userService the service for accessing user data
    @Autowired
    public LevelService(LevelRepository levelRepository, UserService userService) {
        this.levelRepository = levelRepository;
        this.userService = userService;
    }

    /// Creates a level for the given user id.
    /// @spec.requires createLevelDTO and userId are not null.
    /// @spec.effects saves a new Level to the repository with the given title,
    ///               description, and creatorId set to userId.
    /// @param createLevelDTO the DTO containing the title and description of the new level
    /// @param userId the unique identifier of the user
    /// @return the newly created and saved Level
    public Level createLevel(CreateLevelDTO createLevelDTO, String userId) {
        User user = this.userService.getById(userId).orElseThrow(UserNotFoundException::new);
        Level level = new Level(createLevelDTO.title(), createLevelDTO.description(), user);
        return this.levelRepository.save(level);
    }

    /// Clones an existing level for the given user with a unique title.
    /// @spec.requires cloneLevelDTO and user are not null.
    /// @spec.effects if a level with the given sourceLevelId exists and belongs
    ///               to the given user, saves a clone of it to the repository
    ///               with the given user as the new creator and a unique title
    ///               like "title (2)", "title (3)", etc.
    /// @param cloneLevelDTO the DTO containing the id of the level to clone
    /// @param user the user cloning the level
    /// @return a non-empty Optional containing the cloned Level if the source
    ///         level exists and belongs to the user, an empty Optional otherwise
    public Optional<Level> cloneLevel(CloneLevelDTO cloneLevelDTO, User user) {
        return this.levelRepository.findById(cloneLevelDTO.sourceLevelId())
                .filter(level -> level.isOwnedBy(user))
                .map(level -> {
                    String baseTitle = level.getTitle();
                    String uniqueTitle = computeUniqueCloneTitle(baseTitle, user);
                    return this.levelRepository.save(level.cloneFor(user, uniqueTitle));
                });
    }

    /// Computes a unique title for a cloned level by appending (2), (3), etc.
    /// @param baseTitle the original title to clone
    /// @param user the user who will own the clone
    /// @return a unique title not already used by the user
    private String computeUniqueCloneTitle(String baseTitle, User user) {
        Set<String> existingTitles = this.levelRepository.findByCreator(user).stream()
                .map(Level::getTitle)
                .collect(Collectors.toSet());

        String root = baseTitle.replaceAll(" \\(\\d+\\)$", "");

        for (int i = 2; ; i++) {
            String candidate = root + " (" + i + ")";
            if (!existingTitles.contains(candidate)) {
                return candidate;
            }
        }
    }

    // TODO: add jsdoc..
    public void deleteLevel(String userId, String levelId) {
        Level level = this.levelRepository.findById(levelId).orElseThrow(LevelNotFoundException::new);
        level.ensureOwnedBy(userId);
        level.ensureModifiable();
        this.levelRepository.deleteById(levelId);
    }

    /// Retrieves all levels created by the given user, mapped to DTOs.
    /// @param creator the user whose levels to retrieve
    /// @return a list of LevelDTOs for the levels created by the given user
    public List<LevelDTO> getCreatedLevelsByUser(User creator) {
        return this.levelRepository.findByCreator(creator).stream()
                .map(LevelDTO::new)
                .toList();
    }

    /// Updates the properties of an existing unpublished level owned by the given user.
    /// Only the fields present in the DTO will be updated.
    /// @spec.requires user, levelId, and dto are not null.
    /// @spec.modifies the level with the given levelId in the repository.
    /// @spec.effects updates the title, description, and/or clearCondition
    ///               of the level if present in the dto, then saves the updated level.
    /// @param user the user requesting the update
    /// @param levelId the id of the level to update
    /// @param dto the DTO containing the optional new values for title, description, and clear condition
    /// @return the updated and saved level
    public Level updateLevelProperties(User user, String levelId, UpdateLevelDTO dto) {
        Level level = this.levelRepository.findById(levelId).orElseThrow(LevelNotFoundException::new);
        level.ensureOwnedBy(user.getId());
        level.ensureModifiable();

        dto.title().ifPresent(level::setTitle);
        dto.description().ifPresent(level::setDescription);
        dto.clearCondition().ifPresent(level::setClearCondition);
        level.invalidatePublishEligible(user.getId());
        return this.levelRepository.save(level);
    }

    /// Retrieves a level by its unique identifier.
    /// @spec.requires levelId is not null.
    /// @param levelId the id of the level to retrieve
    /// @return a non-empty Optional containing the Level if it exists,
    ///         an empty Optional otherwise
    public Optional<Level> getById(String levelId) {
        return this.levelRepository.findById(levelId);
    }

}
