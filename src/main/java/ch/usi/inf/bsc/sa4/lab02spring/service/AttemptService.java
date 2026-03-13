package ch.usi.inf.bsc.sa4.lab02spring.service;

import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.repository.AttemptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AttemptService {
    private final AttemptRepository attemptRepository;

    /// Constructs a new AttemptService with the given dependency.
    /// @param attemptRepository the repository for accessing attempt data
    @Autowired
    public AttemptService(AttemptRepository attemptRepository) {
        this.attemptRepository = attemptRepository;
    }

    // Returns the number of distinct levels the given user has played.
    /// @spec.requires user is not null.
    /// @param user the user whose played levels to count
    /// @return the number of distinct levels the user has at least one attempt on
    public int getPlayedLevelsCount(User user) {
        return (int) this.attemptRepository.findByUser(user).stream()
                .map(attempt -> attempt.level().getId())
                .distinct()
                .count();
    }

    /// Returns the number of distinct levels the given user has completed.
    /// @spec.requires user is not null.
    /// @param user the user whose completed levels to count
    /// @return the number of distinct levels the user has at least one completed attempt on
    public int getCompletedLevelsCount(User user) {
        return (int) this.attemptRepository.findByUserAndCompletedTrue(user).stream()
                .map(attempt -> attempt.level().getId())
                .distinct()
                .count();
    }
}