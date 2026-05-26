package ch.usi.inf.bsc.sa4.lab02spring.service.level;

import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.repository.AttemptRepository;
import ch.usi.inf.bsc.sa4.lab02spring.repository.AttitudeRepository;
import ch.usi.inf.bsc.sa4.lab02spring.repository.LevelFavoriteRepository;
import ch.usi.inf.bsc.sa4.lab02spring.repository.LevelRepository;
import ch.usi.inf.bsc.sa4.lab02spring.repository.UserRepository;
import ch.usi.inf.bsc.sa4.lab02spring.utils.ForbiddenLevelActionException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.ForbiddenUserException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.LevelNotFoundException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/// Handles level publication state transitions.
@Service
public class LevelPublishService {
    /// Persists publication-state updates on levels.
    private final LevelRepository levelRepository;
    /// Removes attempt entries that belong to edited levels.
    private final AttemptRepository attemptRepository;
    /// Removes favorite entries for edited levels.
    private final LevelFavoriteRepository levelFavoriteRepository;
    /// Removes attitude entries for edited levels.
    private final AttitudeRepository attitudeRepository;
    /// Verifies that referenced users exist.
    private final UserRepository userRepository;

    /// Creates a publish service with repository dependencies.
    ///
    /// @param levelRepository         persists level publication state
    /// @param attemptRepository       persists attempts referencing levels
    /// @param levelFavoriteRepository persists favorite entries referencing levels
    /// @param attitudeRepository      persists attitude entries referencing levels
    /// @param userRepository          resolves users involved in publication
    @Autowired
    public LevelPublishService(final LevelRepository levelRepository,
            final AttemptRepository attemptRepository,
            final LevelFavoriteRepository levelFavoriteRepository,
            final AttitudeRepository attitudeRepository,
            final UserRepository userRepository) {
        this.levelRepository = levelRepository;
        this.attemptRepository = attemptRepository;
        this.levelFavoriteRepository = levelFavoriteRepository;
        this.attitudeRepository = attitudeRepository;
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
    public void publish(final String userId, final String levelId) {
        final Level level = this.levelRepository.findById(levelId).orElseThrow(LevelNotFoundException::new);
        this.userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        level.publish(userId);
        this.levelRepository.save(level);
    }

    /// Unpublishes an existing level owned by the given user.
    /// Unpublishing only hides the level from public listings. It deliberately
    /// keeps attempts, favorites, likes, and dislikes because those records still
    /// describe the same public version. A later edit uses
    /// [#resetLevelAfterEdit(Level, String)] to discard those public records.
    ///
    /// @spec.requires userId and levelId are not null.
    /// @spec.modifies the level identified by levelId.
    /// @spec.effects marks the target level as unpublished and saves the updated
    ///               level.
    /// @param userId  the authenticated user's ID
    /// @param levelId the ID of the level to unpublish
    /// @throws LevelNotFoundException if the level does not exist
    /// @throws ForbiddenUserException if the user is not the owner of the level
    public void unpublishLevel(final String userId, final String levelId) {
        final Level level = this.levelRepository.findById(levelId)
                .orElseThrow(LevelNotFoundException::new);
        level.unpublish(userId);
        this.levelRepository.save(level);
    }

    /// Marks the given level as eligible for publishing on behalf of the given
    /// user.
    ///
    /// @spec.requires level and userId are not null.
    /// @spec.modifies the given level in the repository.
    /// @spec.effects sets the level's publishEligible flag to true and saves it.
    /// @param level  the level to mark as publish eligible
    /// @param userId the unique identifier of the user requesting the validation
    /// @throws ForbiddenUserException if the given user is not the owner of
    ///                                the level
    public void validateLevelPublishEligible(final Level level, final String userId) {
        level.validatePublishEligible(userId);
        this.levelRepository.save(level);
    }

    /// Marks the given level as not eligible for publishing. Applies the change on
    /// behalf of the given user.
    ///
    /// @spec.requires level and userId are not null.
    /// @spec.modifies the given level in the repository.
    /// @spec.effects sets the level's publishEligible flag to false and saves it.
    /// @param level  the level to mark as not publish eligible
    /// @param userId the unique identifier of the user requesting the invalidation
    /// @throws ForbiddenUserException if the given user is not the owner of
    ///                                the level
    public void invalidateLevelPublishEligible(final Level level, final String userId) {
        level.invalidatePublishEligible(userId);
        this.levelRepository.save(level);
    }

    /// Resets all public state that becomes stale after an unpublished level is
    /// edited. Editing changes the level version that attempts, favorites, likes,
    /// and dislikes referred to, so those public engagement records are removed.
    /// The level must also be validated again before it can be published.
    ///
    /// @spec.requires level and userId are not null.
    /// @spec.modifies the given level and public engagement records for it.
    /// @spec.effects clears the publish-eligible flag, deletes attempts, deletes
    ///               favorites, and deletes attitudes for the edited level.
    /// @param level  the edited level whose public records should be reset
    /// @param userId the unique identifier of the user performing the edit
    /// @throws ForbiddenUserException if the given user is not the owner of
    ///                                the level
    public void resetLevelAfterEdit(final Level level, final String userId) {
        level.invalidatePublishEligible(userId);
        this.clearLevelEngagement(level);
    }

    /// Removes every public engagement record that is tied to a level version.
    /// This is used after edits, not after unpublishing, because edits create a
    /// new playable version while unpublishing only hides the existing one.
    ///
    /// @spec.requires level is not null.
    /// @spec.modifies attempts, favorites, and attitudes referencing level.
    /// @spec.effects deletes attempts, favorites, and attitudes for level.
    /// @param level the level whose public engagement should be removed
    private void clearLevelEngagement(final Level level) {
        this.attemptRepository.deleteByLevel(level);
        this.clearPublicReactions(level.getId());
    }

    /// Removes lightweight public reactions for the given level id.
    ///
    /// @spec.requires levelId is not null.
    /// @spec.modifies favorites and attitudes referencing levelId.
    /// @spec.effects deletes favorites and attitudes for levelId.
    /// @param levelId the id of the level whose reactions should be removed
    private void clearPublicReactions(final String levelId) {
        this.levelFavoriteRepository.deleteByLevelId(levelId);
        this.attitudeRepository.deleteByLevelId(levelId);
    }

    /// Removes all engagement records (attempts, favorites, attitudes) for a level
    /// that is being permanently deleted.
    ///
    /// @spec.requires level is not null.
    /// @spec.modifies attempts, favorites, and attitudes referencing level.
    /// @spec.effects deletes all engagement records for the given level.
    /// @param level the level being deleted
    public void clearAllEngagement(final Level level) {
        this.clearLevelEngagement(level);
    }
}
