package ch.usi.inf.bsc.sa4.lab02spring.service;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.AttemptDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.Attempt;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.repository.AttemptRepository;
import ch.usi.inf.bsc.sa4.lab02spring.utils.ForbiddenLevelActionException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.LevelNotFoundException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.UserNotFoundException;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AttemptService {
    private final AttemptRepository attemptRepository;

    /// Constructs a new AttemptService with the given dependency.
    ///
    /// @param attemptRepository the repository for accessing attempt data
    @Autowired
    public AttemptService(AttemptRepository attemptRepository) {
        this.attemptRepository = attemptRepository;
    }

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