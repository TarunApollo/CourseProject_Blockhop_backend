package ch.usi.inf.bsc.sa4.lab02spring.service.level;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.CloneLevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.CreateLevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.CreatedLevelProfileDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.UpdateLevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.repository.AttemptRepository;
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

/// Provides core CRUD-style operations for levels.
@Service
public class LevelService {
    /// Persists and loads level entities.
    private final LevelRepository levelRepository;
    /// Provides play and completion counts for profile summaries.
    private final AttemptRepository attemptRepository;
    /// Resolves users involved in level operations.
    private final UserService userService;
    /// Resets publication state and public engagement after edits.
    private final LevelPublishService levelPublishService;

    /// Constructs a new LevelService with the given dependencies.
    /// @param levelRepository the repository for accessing level data
    /// @param attemptRepository the repository for accessing attempt statistics
    /// @param userService the service for accessing user data
    /// @param levelPublishService resets public publication state after edits
    @Autowired
    public LevelService(
            final LevelRepository levelRepository,
            final AttemptRepository attemptRepository,
            final UserService userService,
            final LevelPublishService levelPublishService) {
        this.levelRepository = levelRepository;
        this.attemptRepository = attemptRepository;
        this.userService = userService;
        this.levelPublishService = levelPublishService;
    }

    /// Creates a level for the given user id.
    /// @spec.requires createLevelDTO and userId are not null.
    /// @spec.effects saves a new Level to the repository with the given title,
    ///               description, and creatorId set to userId.
    /// @param createLevelDTO the DTO containing the new level title and
    ///                       description
    /// @param userId the unique identifier of the user
    /// @return the newly created and saved Level
    public Level createLevel(final CreateLevelDTO createLevelDTO, final String userId) {
        final User user = this.userService.getById(userId).orElseThrow(UserNotFoundException::new);
        final Level level = new Level(createLevelDTO.title(), createLevelDTO.description(), user);
        level.setClearCondition(createLevelDTO.clearCondition());
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
    public Optional<Level> cloneLevel(final CloneLevelDTO cloneLevelDTO, final User user) {
        return this.levelRepository.findById(cloneLevelDTO.sourceLevelId())
                .filter(level -> level.isOwnedBy(user))
                .map(level -> {
                    final String baseTitle = level.getTitle();
                    final String uniqueTitle = computeUniqueCloneTitle(baseTitle, user);
                    return this.levelRepository.save(level.cloneFor(user, uniqueTitle));
                });
    }

    /// Computes a unique title for a cloned level by appending (2), (3), etc.
    /// @param baseTitle the original title to clone
    /// @param user the user who will own the clone
    /// @return a unique title not already used by the user
    private String computeUniqueCloneTitle(final String baseTitle, final User user) {
        final Set<String> existingTitles = this.levelRepository.findByCreator(user).stream()
                .map(Level::getTitle)
                .collect(Collectors.toSet());

        final String root = baseTitle.replaceAll(" \\(\\d+\\)$", "");

        for (int i = 2; ; i++) {
            final String candidate = root + " (" + i + ")";
            if (!existingTitles.contains(candidate)) {
                return candidate;
            }
        }
    }

    /// Deletes an unpublished level owned by the given user.
    ///
    /// @spec.requires userId and levelId are not null.
    /// @spec.modifies removes the referenced level from the repository.
    /// @spec.effects validates ownership and modifiability, then deletes the
    ///               level.
    /// @param userId the unique identifier of the user requesting deletion
    /// @param levelId the id of the level to delete
    /// @throws LevelNotFoundException if the target level does not exist
    public void deleteLevel(final String userId, final String levelId) {
        final Level level = this.levelRepository.findById(levelId).orElseThrow(LevelNotFoundException::new);
        level.ensureOwnedBy(userId);
        level.ensureModifiable();
        this.levelPublishService.clearAllEngagement(level);
        this.levelRepository.deleteById(levelId);
    }

    /// Retrieves all levels created by the given user with public profile
    /// statistics. Public attempt statistics exclude the creator's own attempts
    /// so creators cannot inflate the play or completion counts of their levels.
    /// @param creator the user whose levels to retrieve
    /// @return a list of created level profile DTOs with play and completion counts
    public List<CreatedLevelProfileDTO> getCreatedLevelsByUser(final User creator) {
        return this.levelRepository.findByCreator(creator).stream()
                .map(level -> new CreatedLevelProfileDTO(
                        level,
                        this.attemptRepository.countByLevelAndUserNot(level, level.getCreator()),
                        this.attemptRepository.countByLevelAndUserNotAndCompletedTrue(level, level.getCreator())))
                .toList();
    }

    /// Updates the properties of an existing unpublished level.
    /// Only the fields present in the DTO will be updated.
    /// @spec.requires user, levelId, and dto are not null.
    /// @spec.modifies the level with the given levelId in the repository.
    /// @spec.effects updates the title, description, and/or clearCondition
    ///               of the level if present in the dto, then saves the updated level.
    /// @param user the user requesting the update
    /// @param levelId the id of the level to update
    /// @param dto the DTO containing optional title, description, and clear
    ///            condition updates
    /// @return the updated and saved level
    public Level updateLevelProperties(final User user, final String levelId, final UpdateLevelDTO dto) {
        final Level level = this.levelRepository.findById(levelId).orElseThrow(LevelNotFoundException::new);
        level.ensureOwnedBy(user.getId());
        level.ensureModifiable();

        dto.title().ifPresent(level::setTitle);
        dto.description().ifPresent(level::setDescription);
        dto.clearCondition().ifPresent(level::setClearCondition);
        this.levelPublishService.resetLevelAfterEdit(level, user.getId());
        return this.levelRepository.save(level);
    }

    /// Retrieves a level by its unique identifier.
    /// @spec.requires levelId is not null.
    /// @param levelId the id of the level to retrieve
    /// @return a non-empty Optional containing the Level if it exists,
    ///         an empty Optional otherwise
    public Optional<Level> getById(final String levelId) {
        return this.levelRepository.findById(levelId);
    }

}
