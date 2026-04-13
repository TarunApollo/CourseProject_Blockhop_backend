package ch.usi.inf.bsc.sa4.lab02spring.service;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.CreateAttemptDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.AttemptDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.Attempt;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.repository.AttemptRepository;
import ch.usi.inf.bsc.sa4.lab02spring.repository.LevelRepository;
import ch.usi.inf.bsc.sa4.lab02spring.utils.LevelNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;


@Service
public class AttemptService {
    /// Repository handling attempt persistence.
    private final AttemptRepository attemptRepository;

    /// Repository used to load levels referenced by attempts.
    private final LevelRepository levelRepository;

    /// Constructs a new AttemptService with the given dependency.
    ///
    /// @param attemptRepository the repository for accessing attempt data
    /// @param levelRepository the repository for accessing level data
    @Autowired
    public AttemptService(final AttemptRepository attemptRepository, final LevelRepository levelRepository) {
        this.attemptRepository = attemptRepository;
        this.levelRepository = levelRepository;
    }

    /// Records a new attempt for the given user.
    /// @spec.requires user and dto are not null.
    /// @spec.effects saves a new Attempt to the repository.
    /// @param user the user making the attempt
    /// @param dto contains the level id, completion status, and elapsed time
    /// @return the saved Attempt entity
    @SuppressWarnings("NullAway")
    public Attempt createAttempt(final User user, final CreateAttemptDTO dto) {
        final Level level = levelRepository.findById(dto.levelId()).orElseThrow(LevelNotFoundException::new);
        final Attempt attempt = new Attempt(
                null,
                user,
                ZonedDateTime.now(),
                level,
                dto.completed(),
                dto.timeTaken()
        );
        return attemptRepository.save(attempt);
    }

    /// Returns all attempts for a given user.
    /// @param user the user whose attempts to retrieve
    /// @return the list of attempts for the user
    public List<Attempt> getAttemptsByUser(final User user) {
        return attemptRepository.findByUser(user);
    }

    /// Returns all attempts for a given user on a specific level.
    /// @param user the user whose attempts to retrieve
    /// @param levelId the ID of the level to filter by
    /// @return the list of attempts for the user on the given level
    /// @throws LevelNotFoundException if no level with the given ID exists
    public List<Attempt> getAttemptsByUserAndLevel(final User user, final String levelId) {
        final Level level = levelRepository.findById(levelId).orElseThrow(LevelNotFoundException::new);
        return attemptRepository.findByUserAndLevel(user, level);
    }

    /// Returns a specific attempt by ID, if it belongs to the given user.
    /// @param attemptId the ID of the attempt
    /// @param user the user who should own the attempt
    /// @return an Optional containing the attempt if found and owned by the user
    public Optional<Attempt> getAttemptByIdAndUser(final String attemptId, final User user) {
        return attemptRepository.findByIdAndUser(attemptId, user);
    }

    /// Returns the number of distinct levels the given user has played.
    /// @spec.requires user is not null.
    /// @param user the user whose played levels to count
    /// @return the number of distinct levels the user has at least one attempt on
    public long getPlayedLevelsCount(User user) {
        return this.attemptRepository.countDistinctPlayedLevelsByUser(user);
    }

    public long getCompletedLevelsCount(User user) {
        return this.attemptRepository.countDistinctCompletedLevelsByUser(user);
    }

    /// Sets the first attempt of the given user on the given level to uncompleted.
    ///
    /// @spec.requires user and level are not null.
    /// @spec.modifies the first attempt of the user on the level in the repository.
    /// @spec.effects sets the attempt's completed status to false if an attempt exists;
    ///               does nothing otherwise.
    /// @param user  the user whose attempt to mark as uncompleted
    /// @param level the level on which to mark the attempt as uncompleted
    public void setAttemptUncompleted(User user, Level level){
        List<Attempt> attemptList = this.attemptRepository.findByUserAndLevel(user, level);
        if(!attemptList.isEmpty()){
            this.attemptRepository.save(attemptList.getFirst().setCompleted(false));
        }
    }

    /// Checks whether the given user has a completed attempt on the given level.
    ///
    /// @spec.requires user and level are not null.
    /// @param user  the user to check
    /// @param level the level to check
    /// @return true if the user has at least one completed attempt on the level,
    ///         false otherwise
    public boolean hasCompleted(User user, Level level){
        List<Attempt> attemptList = this.attemptRepository.findByUserAndLevel(user, level);
        return !attemptList.isEmpty() && attemptList.getFirst().completed();
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
    /// @param completed whether the attempt satisfies the level's clear
    ///                  condition
    public void submitAttempt(User user, Level level, AttemptDTO dto, boolean completed){
        @SuppressWarnings("NullAway") Attempt attempt = new Attempt(
                null,
                user,
                dto.timestamp(),
                level,
                false,
                dto.timeTaken()
        );
        if(completed){
            attempt = attempt.setCompleted(true);
        }
        this.attemptRepository.save(attempt);
    }
}
