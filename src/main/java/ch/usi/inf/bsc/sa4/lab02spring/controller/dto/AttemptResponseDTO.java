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
    /// Creates an attempt response DTO from an attempt domain object.
    /// @param attempt the attempt to map into a response DTO
    public AttemptResponseDTO(final Attempt attempt) {
        this(
                attempt.getId(),
                attempt.getUser().getId(),
                attempt.getLevel().getId(),
                attempt.getTimestamp(),
                attempt.getCompleted(),
                attempt.getTimeTaken()
        );
    }
}
