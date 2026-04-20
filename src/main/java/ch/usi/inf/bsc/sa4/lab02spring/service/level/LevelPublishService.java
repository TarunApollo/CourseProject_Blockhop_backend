package ch.usi.inf.bsc.sa4.lab02spring.service.level;

import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.repository.LevelRepository;
import ch.usi.inf.bsc.sa4.lab02spring.repository.UserRepository;
import ch.usi.inf.bsc.sa4.lab02spring.utils.ForbiddenLevelActionException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.ForbiddenUserException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.LevelNotFoundException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LevelPublishService {
    private final LevelRepository levelRepository;
    private final UserRepository userRepository;

    @Autowired
    public LevelPublishService(LevelRepository levelRepository, UserRepository userRepository) {
        this.levelRepository = levelRepository;
        this.userRepository = userRepository;
    }

    /// Publishes the specified level.
    ///
    /// @spec.requires userId and levelId are not null.
    /// @spec.modifies the level identified by levelId.
    /// @spec.effects marks the target level as published and saves the updated
    ///               level.
    /// @param userId  the unique identifier of the user requesting the publish
    /// @param levelId the id of the level to publish
    /// @throws LevelNotFoundException        if no level with the given id exists
    /// @throws UserNotFoundException         if no user with the given id exists
    /// @throws ForbiddenLevelActionException if the user is not the owner of the
    ///                                       level or if the level cannot be
    ///                                       published in its current state
    public void publish(String userId, String levelId) {
        Level level = this.levelRepository.findById(levelId).orElseThrow(LevelNotFoundException::new);
        this.userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        level.publish(userId);
    }

    /// Unpublishes an existing level owned by the given user.
    /// @param userId the authenticated user's ID
    /// @param levelId the ID of the level to unpublish
    /// @throws LevelNotFoundException if the level does not exist
    /// @throws ForbiddenUserException if the user is not the owner of the level
    public void unpublishLevel(String userId, String levelId) {
        Level level = this.levelRepository.findById(levelId)
                .orElseThrow(LevelNotFoundException::new);
        level.unpublish(userId);
    }

    /// Marks the given level as eligible for publishing on behalf of the given user.
    ///
    /// @spec.requires level and userId are not null.
    /// @spec.modifies the given level in the repository.
    /// @spec.effects sets the level's publishEligible flag to true and saves it.
    /// @param level  the level to mark as publish eligible
    /// @param userId the unique identifier of the user requesting the validation
    /// @throws ForbiddenUserException if the given user is not the owner of the level
    public void validateLevelPublishEligible(Level level, String userId) {
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
    public void invalidateLevelPublishEligible(Level level, String userId) {
        level.invalidatePublishEligible(userId);
        this.levelRepository.save(level);
    }

}
