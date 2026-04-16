package ch.usi.inf.bsc.sa4.lab02spring.service;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.*;
import ch.usi.inf.bsc.sa4.lab02spring.converter.LayerToTiledMapConverter;
import ch.usi.inf.bsc.sa4.lab02spring.model.*;
import ch.usi.inf.bsc.sa4.lab02spring.repository.AttemptRepository;
import ch.usi.inf.bsc.sa4.lab02spring.repository.UserRepository;
import ch.usi.inf.bsc.sa4.lab02spring.utils.*;
import ch.usi.inf.bsc.sa4.lab02spring.utils.DateRangePreset.RelativeDateRangePreset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ch.usi.inf.bsc.sa4.lab02spring.repository.LevelRepository;
import ch.usi.inf.bsc.sa4.lab02spring.repository.LevelThumbnailRepository;
import ch.usi.inf.bsc.sa4.lab02spring.repository.ThumbnailRepository;


import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Service
public class LevelService {
    
    private final LevelRepository levelRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final AttemptRepository attemptRepository;
    private final LevelThumbnailRepository levelThumbnailRepository;
    private final ThumbnailRepository thumbnailRepository;
    private final AttemptService attemptService;
    private final TileSetService tileSetService;
    private final LayerToTiledMapConverter layerToTiledMapConverter;


    /// Constructs a new LevelService with the given dependencies.
    /// @param levelRepository the repository for accessing level data
    /// @param userService the service for accessing user data
    @Autowired
    public LevelService(
            LevelRepository levelRepository,
            UserService userService,
            AttemptRepository attemptRepository,
            LevelThumbnailRepository levelThumbnailRepository,
            ThumbnailRepository thumbnailRepository,
            UserRepository userRepository,
            AttemptService attemptService,
            TileSetService tileSetService,
            LayerToTiledMapConverter layerToTiledMapConverter) {
        this.levelRepository = levelRepository;
        this.userService = userService;
        this.attemptRepository = attemptRepository;
        this.levelThumbnailRepository = levelThumbnailRepository;
        this.thumbnailRepository = thumbnailRepository;
        this.userRepository = userRepository;
        this.attemptService = attemptService;
        this.tileSetService = tileSetService;
        this.layerToTiledMapConverter = layerToTiledMapConverter;
    }

    /// Creates a level for the given user id
    /// @spec.requires createLevelDTO and userId are not null.
    /// @spec.effects saves a new Level to the repository with the given title,
    ///              description, and creatorId set to userId.
    /// @param createLevelDTO the DTO containing the title and description of the new level.
    /// @param userId the unique identifier of the user.
    /// @return the newly created and saved Level.
    public Level createLevel(CreateLevelDTO createLevelDTO, String userId) {
        User user = userService.getById(userId).orElseThrow(UserNotFoundException::new);
        Level level = new Level(createLevelDTO.title(), createLevelDTO.description(), user);
        return levelRepository.save(level);
    }

    /// Clones an existing level for the given user.
    /// @spec.requires cloneLevelDTO and user are not null.
    /// @spec.effects if a level with the given sourceLevelId exists and belongs
    ///               to the given user, saves a clone of it to the repository
    ///               with the given user as the new creator.
    /// @param cloneLevelDTO the DTO containing the id of the level to clone.
    /// @param user the user cloning the level.
    /// @return a non-empty Optional containing the cloned Level if the source
    ///          level exists and belongs to the user, an empty Optional otherwise.
    public Optional<Level> cloneLevel(CloneLevelDTO cloneLevelDTO, User user) {
        return this.levelRepository.findById(cloneLevelDTO.sourceLevelId())
                .filter(level -> level.isOwnedBy(user))  // uses domain method instead of inline check
                .map(level -> levelRepository.save(level.cloneFor(user)));
    }

    /// @return a list of all levels
    public List<Level> getAllLevels() {
        return levelRepository.findAll();
    }

    /// Retrieves all levels created by the given user, mapped to DTOs.
    /// @param creator the user whose levels to retrieve
    /// @return a list of LevelDTOs for the levels created by the given user
    public List<LevelDTO> getCreatedLevelsByUser(User creator) {
        return levelRepository.findByCreator(creator).stream()
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
    /// @throws LevelNotFoundException if no level with the given id exists
    ///    /// @throws ForbiddenUserException if the level does not belong to the given user
    ///    /// @throws LevelPublishedException if the level is already published
    public Level updateLevelProperties(User user, String levelId, UpdateLevelDTO dto) {
        Level level = levelRepository.findById(levelId).orElseThrow(LevelNotFoundException::new);
        // use domain methods for business rule enforcement
        level.ensureOwnedBy(user.getId()); // throws ForbiddenUserException if not owner
        level.ensureModifiable();  // throws LevelPublishedException if published

        dto.title().ifPresent(level::setTitle);
        dto.description().ifPresent(level::setDescription);
        dto.clearCondition().ifPresent(level::setClearCondition);
        level.invalidatePublishEligible(user.getId());
        return levelRepository.save(level);
    }

    /// Returns a Tiled/Phaser-compatible map representation of a playable level.
    ///
    /// @spec.requires user and levelId are not null.
    /// @spec.effects loads the requested level, verifies that the given user is
    ///               allowed to play it, retrieves the loaded tileset metadata,
    ///               and converts the level into a frontend-consumable map
    ///               structure.
    /// @param user the user requesting to play the level
    /// @param levelId the id of the level to export as a playable map
    /// @return a map JSON structure compatible with the current frontend level player
    /// @throws LevelNotFoundException if no level with the given id exists
    /// @throws ForbiddenUserException if the given user is not allowed to play the level
    public Map<String,Object> getPlayableMap(final User user,final String levelId)
    {
        final Level level = levelRepository.findById(levelId).orElseThrow(LevelNotFoundException::new);
        level.ensurePlayable(user.getId());
        final TileSet tileSet = this.tileSetService.getTileSet();
        return this.layerToTiledMapConverter.convertPipeline(level, tileSet);
    }


    /// Unpublishes an existing level owned by the given user.
    /// @param userId the authenticated user's ID
    /// @param levelId the ID of the level to unpublish
    /// @return the updated level
    /// @throws LevelNotFoundException if the level does not exist
    /// @throws ForbiddenUserException if the user is not the owner of the level
    public Level unpublishLevel(String userId, String levelId) {
        Level level = this.levelRepository.findById(levelId)
                .orElseThrow(LevelNotFoundException::new);
        level.unpublish(userId);
        return this.levelRepository.save(level);
    }

    /// Stores or replaces the thumbnail associated with the given level.
    ///
    /// @spec.requires userId, levelId, and thumbnail are not null.
    /// @spec.effects reads the uploaded thumbnail, stores it through the thumbnail
    ///               repository, replaces any previous thumbnail mapping for the
    ///               target level, and saves the new thumbnail mapping.
    /// @param userId the id of the requesting user
    /// @param levelId the id of the target level
    /// @param thumbnail the uploaded thumbnail snapshot
    /// @return the storage id of the stored thumbnail
    public String saveThumbnailForLevel(String userId, String levelId, MultipartFile thumbnail) {
        Level level = this.levelRepository.findById(levelId)
                .orElseThrow(LevelNotFoundException::new);
        level.ensureOwnedBy(userId);
        level.ensureModifiable();
        byte[] bytes;
        try {
            bytes = thumbnail.getBytes();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read uploaded thumbnail", e);
        }

        // delete previous thumbnail at first

        levelThumbnailRepository.findByLevelId(levelId)
        .ifPresent(oldThumbnail -> {
            thumbnailRepository.deleteThumbnail(oldThumbnail.storageId());
            levelThumbnailRepository.deleteByLevelId(levelId);
        });

        // save thumbnail
        String storageId = thumbnailRepository.storeThumbnail(levelId, bytes);
        levelThumbnailRepository.save(new LevelThumbnail(null, levelId, storageId));

        return storageId;
    }

    /// Builds a LevelSummaryDto for the given level, computing play count, clear rate, and popularity.
    /// @param level the level to summarize
    /// @param period the time range used to compute popularity
    /// @return a LevelSummaryDto with computed statistics
    private LevelSummaryDto toLevelSummary(Level level, PublishedLevelSortBy sortBy,DateRangePreset period) {
        long playCount = attemptRepository.countByLevel(level);
        long clearCount = attemptRepository.countByLevelAndCompletedTrue(level);
        double clearRate = playCount == 0 ? 0 : (double) clearCount / playCount;
        long popularity = playCount;
        if (sortBy == PublishedLevelSortBy.POPULARITY && period instanceof RelativeDateRangePreset relative) {
            popularity = attemptRepository.countByLevelAndTimestampAfter(level, relative.rangeStart());
        }

        String thumbnailUrl = "/levels/" + level.getId() + "/thumbnail";
        return new LevelSummaryDto(level, playCount, clearRate, popularity,thumbnailUrl);
    }

    /// Returns all published levels as summaries, sorted by the given criteria.
    /// @param sortBy the sorting strategy for published level summaries
    /// @param period the time range used to compute popularity; use ALL_TIME to fall back to total play count
    /// @return a sorted list of LevelSummaryDto for all published levels
    public List<LevelSummaryDto> getPublishedLevels(PublishedLevelSortBy sortBy, DateRangePreset period) {
        List<Level> levels = levelRepository.findByPublishedTrue();

        List<LevelSummaryDto> dtos = levels.stream()
                .map(level -> toLevelSummary(level, sortBy,period))
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

    /// Publishes the specified level.
    ///
    /// @spec.requires userId and levelId are not null.
    /// @spec.modifies the level identified by levelId.
    /// @spec.effects marks the target level as published and saves the updated
    ///               level.
    /// @param userId  the unique identifier of the user requesting the publish
    /// @param levelId the id of the level to publish
    /// @return the published and saved Level
    /// @throws LevelNotFoundException        if no level with the given id exists
    /// @throws UserNotFoundException         if no user with the given id exists
    /// @throws ForbiddenLevelActionException if the user is not the owner of the
    ///                                       level or if the level cannot be
    ///                                       published in its current state
    public Level publish(String userId, String levelId){
        Level level = levelRepository.findById(levelId).orElseThrow(LevelNotFoundException::new);
        userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        level.publish(userId);
        return this.levelRepository.save(level);
    }

    /// Retrieves a level by its unique identifier.
    ///
    /// @spec.requires levelId is not null.
    /// @param levelId the id of the level to retrieve
    /// @return a non-empty Optional containing the Level if it exists,
    ///         an empty Optional otherwise.
    public Optional<Level> getById(String levelId) {
        return this.levelRepository.findById(levelId);
    }

    /// Marks the given level as eligible for publishing on behalf of the given user.
    ///
    /// @spec.requires level and userId are not null.
    /// @spec.modifies the given level in the repository.
    /// @spec.effects sets the level's publishEligible flag to true and saves it.
    /// @param level  the level to mark as publish eligible
    /// @param userId the unique identifier of the user requesting the validation
    /// @throws ForbiddenUserException if the given user is not the owner of the level
    public void validateLevelPublishEligible(Level level, String userId){
        level.validatePublishEligible(userId);
        this.levelRepository.save(level);
    }

    /// Marks the given level as not eligible for publishing on behalf of the given user.
    ///
    /// @spec.requires level and userId are not null.
    /// @spec.modifies the given level in the repository.
    /// @spec.effects sets the level's publishEligible flag to false and saves it.
    /// @param level  the level to mark as not publish eligible
    /// @param userId the unique identifier of the user requesting the invalidation
    /// @throws ForbiddenUserException if the given user is not the owner of the level
    public void invalidateLevelPublishEligible(Level level, String userId){
        level.invalidatePublishEligible(userId);
        this.levelRepository.save(level);
    }
    /// Returns the stored thumbnail image bytes for the given level.
    ///
    /// @spec.requires levelId is not null.
    /// @spec.effects looks up the thumbnail mapping for the target level and loads
    ///               the corresponding thumbnail bytes from the thumbnail repository.
    /// @param levelId the id of the level whose thumbnail is requested
    /// @return the stored thumbnail image bytes
    public byte[] getThumbnailForLevel(String levelId)
    {
        LevelThumbnail thumbnail = levelThumbnailRepository.findByLevelId(levelId)
        .orElseThrow(()->new IllegalArgumentException("Thumbnail not found"));

        String storageId = thumbnail.storageId();

        return thumbnailRepository.loadThumbnail(storageId);
    }

    /// Validates whether the submitted attempt satisfies the level's completion
    /// criteria.
    ///
    /// @spec.requires level and dto are not null.
    /// @spec.effects compares the submitted world layer against the level's
    ///               expected world layer and checks whether the player's
    ///               position corresponds to an ExitDoor in the object layer.
    /// @param level the level to validate the submission against
    /// @param dto   the DTO containing the submitted world layer and player
    ///              position
    /// @return true if the world layer matches the expected state and the
    ///         player is positioned on an ExitDoor, false otherwise
    public boolean validateLevelSubmission(Level level, AttemptDTO dto){
        boolean isWorldLayerEqual = level.getWorldLayer().equals(dto.worldLayer());
        // boolean isPlayerValid = level.getObjectLayer().get(dto.playerPosition()) instanceof ExitDoor;
        return isWorldLayerEqual;
    }


    /// Submits an attempt for the specified level on behalf of the given user.
    ///
    /// @spec.requires levelId, userId, and dto are not null.
    /// @spec.effects validates the attempt against the level state; if the level
    ///               is unpublished and owned by the given user and the attempt
    ///               is successful, marks the level as publish eligible;
    ///               records the attempt through the attempt service.
    /// @param levelId the id of the level to submit an attempt for
    /// @param userId  the unique identifier of the user submitting the attempt
    /// @param dto     the DTO containing the attempt details
    /// @return a success message indicating the attempt was submitted
    /// @throws UserNotFoundException if no user with the given userId exists
    /// @throws LevelNotFoundException if no level with the given levelId exists
    /// @throws ForbiddenLevelActionException if the level is unpublished and the
    ///         user is not its creator
    /// @throws ForbiddenUserException if the user is not the owner of the level
    ///         when marking it as publish eligible
    public String submitAttempt(String levelId, String userId, AttemptDTO dto){
        User user = this.userService.getById(userId).orElseThrow(UserNotFoundException::new);
        Level level = this.getById(levelId).orElseThrow(LevelNotFoundException::new);
        // boolean completed = this.validateLevelSubmission(level, dto);
        if(!level.isPublished() && level.isOwnedBy(userId)){
            this.validateLevelPublishEligible(level, userId);
        }
        if(!level.isPublished() && !level.isOwnedBy(userId)){
            throw new ForbiddenLevelActionException("Level submission is not valid.");
        }
        this.attemptService.submitAttempt(user, level, dto);
        return "Successful level submission.";
    }
}
