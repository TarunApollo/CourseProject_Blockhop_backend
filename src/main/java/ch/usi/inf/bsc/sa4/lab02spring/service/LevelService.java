package ch.usi.inf.bsc.sa4.lab02spring.service;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.*;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.repository.AttemptRepository;
import ch.usi.inf.bsc.sa4.lab02spring.utils.DateRangePreset;
import ch.usi.inf.bsc.sa4.lab02spring.utils.DateRangePreset.RelativeDateRangePreset;
import ch.usi.inf.bsc.sa4.lab02spring.utils.ForbiddenUserException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.LevelNotFoundException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.LevelPublishedException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.PublishedLevelSortBy;
import ch.usi.inf.bsc.sa4.lab02spring.utils.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.repository.LevelRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class LevelService {
    private final LevelRepository levelRepository;
    private final UserService userService;
    private final AttemptRepository attemptRepository;

    /// Constructs a new LevelService with the given dependencies.
    /// 
    /// @param levelRepository   the repository for accessing level data
    /// @param userService       the service for accessing user data
    /// @param attemptRepository the repository for accessing attempt data
    @Autowired
    public LevelService(LevelRepository levelRepository, UserService userService, AttemptRepository attemptRepository) {
        this.levelRepository = levelRepository;
        this.userService = userService;
        this.attemptRepository = attemptRepository;
    }

    /// Creates a level for the given user id
    /// 
    /// @spec.requires createLevelDTO and userId are not null.
    /// @spec.effects saves a new Level to the repository with the given title,
    ///               description, and creatorId set to userId.
    /// @param createLevelDTO the DTO containing the title and description of the new
    ///                       level.
    /// @param userId         the unique identifier of the user.
    /// @return the newly created and saved Level.
    public Level createLevel(CreateLevelDTO createLevelDTO, String userId) {
        User user = userService.getById(userId).orElseThrow(UserNotFoundException::new);
        Level level = new Level(createLevelDTO.title(), createLevelDTO.description(), user);
        return levelRepository.save(level);
    }

    /// Clones an existing level for the given user.
    /// 
    /// @spec.requires cloneLevelDTO and user are not null.
    /// @spec.effects if a level with the given sourceLevelId exists and belongs to
    ///               the given user, saves a clone of it to the repository with the
    ///               given user as the new creator.
    /// @param cloneLevelDTO the DTO containing the id of the level to clone.
    /// @param user          the user cloning the level.
    /// @return a non-empty Optional containing the cloned Level if the source level
    ///         exists and belongs to the user, an empty Optional otherwise.
    public Optional<Level> cloneLevel(CloneLevelDTO cloneLevelDTO, User user) {
        return this.levelRepository.findById(cloneLevelDTO.sourceLevelId())
                .filter(level -> level.isOwnedBy(user)) // uses domain method instead of inline check
                .map(level -> levelRepository.save(level.cloneFor(user)));
    }


    /// Retrieves all levels created by the given user, mapped to DTOs.
    /// 
    /// @param creator the user whose levels to retrieve
    /// @return a list of LevelDTOs for the levels created by the given user
    public List<LevelDTO> getCreatedLevelsByUser(User creator) {
        return levelRepository.findByCreator(creator).stream()
                .map(LevelDTO::new)
                .toList();
    }

    /// Updates the properties of an existing unpublished level owned by the given
    /// user. Only the fields present in the DTO will be updated.
    /// 
    /// @spec.requires user, levelId, and dto are not null.
    /// @spec.modifies the level with the given levelId in the repository.
    /// @spec.effects updates the title, description, and/or clearCondition of the
    ///               level if present in the dto, then saves the updated level.
    /// @param user    the user requesting the update
    /// @param levelId the id of the level to update
    /// @param dto     the DTO containing the optional new values for title,
    ///                description, and clear condition
    /// @return the updated and saved level
    /// @throws LevelNotFoundException  if no level with the given id exists ///
    /// @throws ForbiddenUserException  if the level does not belong to the given
    ///                                 user ///
    /// @throws LevelPublishedException if the level is already published
    public Level updateLevelProperties(User user, String levelId, UpdateLevelDTO dto) {
        Level level = levelRepository.findById(levelId).orElseThrow(LevelNotFoundException::new);
        // use domain methods for business rule enforcement
        level.ensureOwnedBy(user.getId()); // throws ForbiddenUserException if not owner
        level.ensureModifiable(); // throws LevelPublishedException if published

        dto.title().ifPresent(level::setTitle);
        dto.description().ifPresent(level::setDescription);
        dto.clearCondition().ifPresent(level::setClearCondition);
        return levelRepository.save(level);
    }

    /// Builds a LevelSummaryDto for the given level, computing play count, clear rate, and popularity.
    /// @param level the level to summarize
    /// @param period the time range used to compute popularity
    /// @return a LevelSummaryDto with computed statistics
    private LevelSummaryDto toLevelSummary(Level level, DateRangePreset period) {
        long playCount = attemptRepository.countByLevel(level);
        long clearCount = attemptRepository.countByLevelAndCompletedTrue(level);
        double clearRate = playCount == 0 ? 0 : (double) clearCount / playCount;
        long popularity;
        if (period instanceof RelativeDateRangePreset relative) {
            popularity = attemptRepository.countByLevelAndTimestampAfter(level, relative.rangeStart());
        } else {
            popularity = playCount;
        }
        return new LevelSummaryDto(level, playCount, clearRate, popularity);
    }

    /// Returns all published levels as summaries, sorted by the given criteria.
    /// @param sortBy the sorting strategy for published level summaries
    /// @param period the time range used to compute popularity; use ALL_TIME to fall back to total play count
    /// @return a sorted list of LevelSummaryDto for all published levels
    public List<LevelSummaryDto> getPublishedLevels(PublishedLevelSortBy sortBy, DateRangePreset period) {
        List<Level> levels = levelRepository.findByPublishedTrue();

        List<LevelSummaryDto> dtos = levels.stream()
                .map(level -> toLevelSummary(level, period))
                .toList();

        return switch (sortBy) {
            case CLEAR_RATE -> dtos.stream()
                    .sorted(Comparator.comparingDouble(LevelSummaryDto::clearRate).reversed())
                    .toList();
            case POPULARITY -> dtos.stream()
                    .sorted(Comparator.comparingLong(LevelSummaryDto::popularity).reversed())
                    .toList();
        };
    }
}
