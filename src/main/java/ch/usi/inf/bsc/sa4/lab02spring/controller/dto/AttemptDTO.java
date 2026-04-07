package ch.usi.inf.bsc.sa4.lab02spring.controller.dto;

import ch.usi.inf.bsc.sa4.lab02spring.model.Attempt;

import java.time.Duration;
import java.time.ZonedDateTime;

/// Response DTO for an attempt.
/// @param id the unique identifier of the attempt
/// @param userId the ID of the user who made the attempt
/// @param levelId the ID of the level the attempt was made on
/// @param timestamp when the attempt was recorded
/// @param completed whether the attempt was completed successfully
/// @param timeTaken the duration of the attempt
public record AttemptDTO(
        String id,
        String userId,
        String levelId,
        ZonedDateTime timestamp,
        boolean completed,
        Duration timeTaken
) {
    /// Constructs an AttemptDTO from the given Attempt entity.
    /// @param attempt the attempt to convert into a DTO
    public AttemptDTO(Attempt attempt) {
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