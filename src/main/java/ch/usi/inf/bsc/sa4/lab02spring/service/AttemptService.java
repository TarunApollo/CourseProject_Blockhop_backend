package ch.usi.inf.bsc.sa4.lab02spring.service;

import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.repository.AttemptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

}