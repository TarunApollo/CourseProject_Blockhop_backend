package ch.usi.inf.bsc.sa4.lab02spring.service;

import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.repository.AttemptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AttemptService {
    private final AttemptRepository attemptRepository;

    @Autowired
    public AttemptService(AttemptRepository attemptRepository) {
        this.attemptRepository = attemptRepository;
    }

    public int getPlayedLevelsCount(User user) {
        return (int) this.attemptRepository.findByUser(user).stream()
                .map(attempt -> attempt.level().getId())
                .distinct()
                .count();
    }

    public int getCompletedLevelsCount(User user) {
        return (int) this.attemptRepository.findByUserAndCompletedTrue(user).stream()
                .map(attempt -> attempt.level().getId())
                .distinct()
                .count();
    }
}