package ch.usi.inf.bsc.sa4.lab02spring.service;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.CreateAttemptDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.AttemptDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.Attempt;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.repository.AttemptRepository;
import ch.usi.inf.bsc.sa4.lab02spring.utils.LevelNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

/// Service handling creation and querying of player attempts.
@Service
public class AttemptService {
    /// Repository handling attempt persistence.
    private final AttemptRepository attemptRepository;

    /// Constructs a new AttemptService with the given dependency.
    ///
    /// @param attemptRepository the repository for accessing attempt data
    @Autowired
    public AttemptService(final AttemptRepository attemptRepository) {
        this.attemptRepository = attemptRepository;
    }

    /// Returns the number of distinct levels the given user has played.
    /// @spec.requires user is not null.
    /// @param user the user whose played levels to count
    /// @return the number of distinct levels the user has at least one attempt on
    public long getPlayedLevelsCount(User user) {
        return this.attemptRepository.countDistinctPlayedLevelsByUser(user);
    }

    /// Returns the number of distinct levels the given user has completed.
    /// @param user the user whose completed levels to count
    /// @return the number of distinct levels the user has completed at least once
    public long getCompletedLevelsCount(User user) {
        return this.attemptRepository.countDistinctCompletedLevelsByUser(user);
    }


    /// Records a new attempt for the given user on the given level.
    ///
    /// @spec.requires user, level, and dto are not null.
    /// @spec.effects creates a new Attempt from the provided data and saves it
    ///               to the repository; if completed is true, the attempt is
    ///               marked as completed before saving.
    /// @param user      the user who performed the attempt
    /// @param level     the level the attempt was made on
    /// @param dto       the DTO containing the attempt timestamp and time taken
    ///                  condition
    public void submitAttempt(User user, Level level, AttemptDTO dto){
        Attempt attempt = new Attempt(
                user,
                dto.timestamp(),
                level,
                dto.completed(),
                dto.timeTaken()
        );
        this.attemptRepository.save(attempt);
    }
}
