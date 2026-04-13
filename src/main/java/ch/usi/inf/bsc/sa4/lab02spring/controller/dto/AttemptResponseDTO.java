package ch.usi.inf.bsc.sa4.lab02spring.controller.dto;

import ch.usi.inf.bsc.sa4.lab02spring.model.Attempt;

import java.time.Duration;
import java.time.ZonedDateTime;

public record AttemptResponseDTO(
        String id,
        String userId,
        String levelId,
        ZonedDateTime timestamp,
        boolean completed,
        Duration timeTaken
) {
    public AttemptResponseDTO(Attempt attempt) {
        this(
                attempt.id(),
                attempt.user().getId(),
                attempt.level().getId(),
                attempt.timestamp(),
                attempt.completed(),
                attempt.timeTaken()
        );
    }
}
