package ch.usi.inf.bsc.sa4.lab02spring.service.level;

import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.LevelAttitude;
import ch.usi.inf.bsc.sa4.lab02spring.model.LevelAttitudeType;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.repository.AttitudeRepository;
import ch.usi.inf.bsc.sa4.lab02spring.repository.LevelRepository;
import ch.usi.inf.bsc.sa4.lab02spring.service.UserService;
import ch.usi.inf.bsc.sa4.lab02spring.utils.LevelNotFoundException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.UserNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/// Provides operations for managing level attitudes (likes and dislikes).
@Service
public class LevelAttitudeService {
    /// Persists and loads level entities.
    private final LevelRepository levelRepository;
    /// Persists and loads level attitude entities.
    private final AttitudeRepository attitudeRepository;
    /// Resolves users involved in attitude operations.
    private final UserService userService;

    /// Constructs a new LevelAttitudeService with the given dependencies.
    /// 
    /// @param levelRepository    the repository for accessing level data
    /// @param attitudeRepository the repository for accessing attitude data
    /// @param userService        the service for accessing user data
    @Autowired
    public LevelAttitudeService(
            final LevelRepository levelRepository,
            final AttitudeRepository attitudeRepository,
            final UserService userService) {
        this.levelRepository = levelRepository;
        this.attitudeRepository = attitudeRepository;
        this.userService = userService;
    }

    /// Sets or updates a user's attitude towards a level. If the user already has
    /// an attitude for this level, it will be updated, otherwise, a new attitude
    /// record is created.
    ///
    /// @spec.requires userId and levelId are not null.
    /// @spec.effects creates a new LevelAttitude or updates an existing one in the
    ///               repository.
    /// @param userId       the unique identifier of the user
    /// @param levelId      the unique identifier of the level
    /// @param attitudeType the attitude type (like or dislike)
    /// @throws UserNotFoundException  if the user does not exist
    /// @throws LevelNotFoundException if the level does not exist
    public void setAttitude(final String userId, final String levelId, final LevelAttitudeType attitudeType) {
        final User user = this.userService.getById(userId).orElseThrow(UserNotFoundException::new);
        final Level level = this.levelRepository.findById(levelId).orElseThrow(LevelNotFoundException::new);
        level.ensurePublished();
        final Optional<LevelAttitude> existing = this.attitudeRepository.findByLevelAndUser(level, user);

        if (existing.isPresent()) {
            final LevelAttitude att = existing.get();
            att.setAttitude(attitudeType);
            this.attitudeRepository.save(att);
        } else {
            final LevelAttitude newAttitude = new LevelAttitude(user, level, attitudeType);
            this.attitudeRepository.save(newAttitude);
        }
    }

    /// Deletes a user's attitude towards a level if it exists.
    ///
    /// @spec.requires userId and levelId are not null.
    /// @spec.modifies removes the user's attitude record for the level from the
    ///                repository if present.
    /// @spec.effects deletes the attitude if found; does nothing if no attitude
    ///               exists.
    /// @param userId  the unique identifier of the user
    /// @param levelId the unique identifier of the level
    /// @throws UserNotFoundException  if the user does not exist
    /// @throws LevelNotFoundException if the level does not exist
    public void deleteAttitude(final String userId, final String levelId) {
        final User user = this.userService.getById(userId).orElseThrow(UserNotFoundException::new);
        final Level level = this.levelRepository.findById(levelId).orElseThrow(LevelNotFoundException::new);

        // Find and delete the user's attitude for this level, if it exists
        this.attitudeRepository.findByLevelAndUser(level, user)
                .ifPresent(a -> this.attitudeRepository.deleteByLevelAndUser(level, user));
    }
}
