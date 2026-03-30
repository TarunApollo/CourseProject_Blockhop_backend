package ch.usi.inf.bsc.sa4.lab02spring.service;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.AttemptDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.Attempt;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.repository.AttemptRepository;
import ch.usi.inf.bsc.sa4.lab02spring.utils.LevelNotFoundException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.UserNotFoundException;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AttemptService {
    private final AttemptRepository attemptRepository;
    private final LevelService levelService;
    private final UserService userService;

    /// Constructs a new AttemptService with the given dependency.
    /// @param attemptRepository the repository for accessing attempt data
    @Autowired
    public AttemptService(AttemptRepository attemptRepository, LevelService levelService, UserService userService) {
        this.attemptRepository = attemptRepository;
        this.levelService = levelService;
        this.userService = userService;
    }

    // Returns the number of distinct levels the given user has played.
    /// @spec.requires user is not null.
    /// @param user the user whose played levels to count
    /// @return the number of distinct levels the user has at least one attempt on
    public long getPlayedLevelsCount(User user) {
        return this.attemptRepository.findByUser(user).stream()
                .map(attempt -> attempt.level().getId())
                .distinct()
                .count();
    }

    /// Returns the number of distinct levels the given user has completed.
    /// @spec.requires user is not null.
    /// @param user the user whose completed levels to count
    /// @return the number of distinct levels the user has at least one completed attempt on
    public long getCompletedLevelsCount(User user) {
        return this.attemptRepository.findByUserAndCompletedTrue(user).stream()
                .map(attempt -> attempt.level().getId())
                .distinct()
                .count();
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

    public Attempt submitAttempt(String levelId, String userId, AttemptDTO dto){
        User user = this.userService.getById(userId).orElseThrow(UserNotFoundException::new);
        Level level = this.levelService.getById(levelId).orElseThrow(LevelNotFoundException::new);;
        @SuppressWarnings("NullAway") Attempt attempt = new Attempt(
                null,
                user,
                dto.timestamp(),
                level,
                false,
                dto.timeTaken()
        );

        if(this.levelService.validateLevelSubmission(level, dto)){
            attempt = attempt.setCompleted(true);
            this.levelService.modifyLevelPublishEligible(level, userId, true);
        }
        return this.attemptRepository.save(attempt);
    }
}