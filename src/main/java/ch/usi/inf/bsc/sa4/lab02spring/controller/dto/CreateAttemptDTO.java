package ch.usi.inf.bsc.sa4.lab02spring.controller.dto;

import java.time.Duration;

/// Request DTO for recording a new attempt on a level.
/// @param levelId the ID of the level being attempted
/// @param completed whether the attempt resulted in completion
/// @param timeTaken the duration of the attempt
public record CreateAttemptDTO(String levelId, boolean completed, Duration timeTaken) {
}