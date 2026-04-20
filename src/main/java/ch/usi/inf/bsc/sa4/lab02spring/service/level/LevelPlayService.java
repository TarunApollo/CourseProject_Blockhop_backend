package ch.usi.inf.bsc.sa4.lab02spring.service.level;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.AttemptDTO;
import ch.usi.inf.bsc.sa4.lab02spring.converter.LayerToTiledMapConverter;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.TileSet;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.repository.LevelRepository;
import ch.usi.inf.bsc.sa4.lab02spring.service.AttemptService;
import ch.usi.inf.bsc.sa4.lab02spring.service.TileSetService;
import ch.usi.inf.bsc.sa4.lab02spring.service.UserService;
import ch.usi.inf.bsc.sa4.lab02spring.utils.ForbiddenLevelActionException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.ForbiddenUserException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.LevelNotFoundException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class LevelPlayService {
    private final LevelRepository levelRepository;
    private final UserService userService;
    private final AttemptService attemptService;
    private final TileSetService tileSetService;
    private final LayerToTiledMapConverter layerToTiledMapConverter;
    private final LevelPublishService levelPublishService;

    @Autowired
    public LevelPlayService(
            LevelRepository levelRepository,
            UserService userService,
            AttemptService attemptService,
            TileSetService tileSetService,
            LayerToTiledMapConverter layerToTiledMapConverter,
            LevelPublishService levelPublishService) {
        this.levelRepository = levelRepository;
        this.userService = userService;
        this.attemptService = attemptService;
        this.tileSetService = tileSetService;
        this.layerToTiledMapConverter = layerToTiledMapConverter;
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
    public String handleLevelSubmission(String levelId, String userId, AttemptDTO dto) {
        User user = this.userService.getById(userId).orElseThrow(UserNotFoundException::new);
        Level level = this.levelRepository.findById(levelId).orElseThrow(LevelNotFoundException::new);
        if (!level.isPublished() && level.isOwnedBy(userId) && dto.completed()) {
            this.levelPublishService.validateLevelPublishEligible(level, userId);
        }
        if (!level.isPublished() && !level.isOwnedBy(userId)) {
            throw new ForbiddenLevelActionException("Level submission is not valid.");
        }
        this.attemptService.submitAttempt(user, level, dto);
        return "Successful level submission.";
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
    public Map<String, Object> getPlayableMap(final User user, final String levelId) {
        final Level level = this.levelRepository.findById(levelId).orElseThrow(LevelNotFoundException::new);
        level.ensurePlayable(user.getId());
        final TileSet tileSet = this.tileSetService.getTileSet();
        return this.layerToTiledMapConverter.convertPipeline(level, tileSet);
    }
}
