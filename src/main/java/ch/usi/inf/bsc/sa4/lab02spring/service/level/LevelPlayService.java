package ch.usi.inf.bsc.sa4.lab02spring.service.level;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.AttemptDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.Attempt;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.TileSet;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.repository.LevelRepository;
import ch.usi.inf.bsc.sa4.lab02spring.service.AttemptService;
import ch.usi.inf.bsc.sa4.lab02spring.service.TileSetService;
import ch.usi.inf.bsc.sa4.lab02spring.service.UserService;
import ch.usi.inf.bsc.sa4.lab02spring.service.anticheat.AntiCheatLog;
import ch.usi.inf.bsc.sa4.lab02spring.utils.ForbiddenLevelActionException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.ForbiddenUserException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.LevelNotFoundException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.UserNotFoundException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.converter.LayerToTiledMapConverter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/// Coordinates playable map retrieval and attempt submission.
@Service
public class LevelPlayService {
    /// Loads levels for play requests.
    private final LevelRepository levelRepository;
    /// Resolves the active user for play operations.
    private final UserService userService;
    /// Persists submitted attempts.
    private final AttemptService attemptService;
    /// Loads the tileset used in exported maps.
    private final TileSetService tileSetService;
    /// Updates publish eligibility after successful submissions.
    private final LevelPublishService levelPublishService;

    /// Creates a level play service with its collaborators.
    ///
    /// @param levelRepository loads levels involved in play flows
    /// @param userService resolves users by identifier
    /// @param attemptService stores submitted attempts
    /// @param tileSetService loads tileset metadata
    /// @param levelPublishService updates publish eligibility
    @Autowired
    public LevelPlayService(
            final LevelRepository levelRepository,
            final UserService userService,
            final AttemptService attemptService,
            final TileSetService tileSetService,
            final LevelPublishService levelPublishService) {
        this.levelRepository = levelRepository;
        this.userService = userService;
        this.attemptService = attemptService;
        this.tileSetService = tileSetService;
        this.levelPublishService = levelPublishService;
    }

    /// Submits an attempt for the specified level on behalf of the given user.
    ///
    /// @spec.requires levelId, userId, and dto are not null.
    /// @spec.effects validates the attempt against the level state; if the level
    ///               is unpublished and owned by the given user and the attempt
    ///               is successful, marks the level as publish eligible;
    ///               records the attempt through the attempt service.
    /// @param levelId the id of the level to submit an attempt for
    /// @param userId the unique identifier of the user submitting the attempt
    /// @param dto the DTO containing the attempt details
    /// @return a success message indicating the attempt was submitted
    /// @throws UserNotFoundException if no user with the given userId exists
    /// @throws LevelNotFoundException if no level with the given levelId exists
    /// @throws ForbiddenLevelActionException if the level is unpublished and the
    ///         user is not its creator
    /// @throws ForbiddenUserException if the user is not the owner of the level
    ///         when marking it as publish eligible
    public Attempt handleLevelSubmission(final String levelId, final String userId, final AttemptDTO dto) {
        final User user = this.userService.getById(userId).orElseThrow(UserNotFoundException::new);
        final Level level = this.levelRepository.findById(levelId).orElseThrow(LevelNotFoundException::new);
        if (!level.isPublished() && level.isOwnedBy(userId) && dto.completed()) {
            this.levelPublishService.validateLevelPublishEligible(level, userId);
        }
        if (!level.isPublished() && !level.isOwnedBy(userId)) {
            throw new ForbiddenLevelActionException("Level submission is not valid.");
        }
        return this.attemptService.submitAttempt(user, level, dto);
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
    /// @return a map JSON structure for the current frontend level player
    /// @throws LevelNotFoundException if no level with the given id exists
    /// @throws ForbiddenUserException if the given user is not allowed to play
    ///         the level
    public Map<String, Object> getPlayableMap(final User user, final String levelId) {
        final Level level = this.levelRepository.findById(levelId).orElseThrow(LevelNotFoundException::new);
        level.ensurePlayable(user.getId());
        AntiCheatLog.levelEntered(user.getId(), levelId);
        final TileSet tileSet = this.tileSetService.getTileSet();
        return LayerToTiledMapConverter.convertPipeline(level, tileSet, this.tileSetService);
    }
}
