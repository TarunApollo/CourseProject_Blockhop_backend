package ch.usi.inf.bsc.sa4.lab02spring.controller.dto;

import ch.usi.inf.bsc.sa4.lab02spring.model.LevelAttitudeType;
import com.fasterxml.jackson.annotation.JsonProperty;

/// DTO for setting or updating a level attitude (like/dislike).
/// @param attitude the attitude type (like or dislike)
public record SetLevelAttitudeDTO(
        @JsonProperty("attitude") LevelAttitudeType attitude
) {
}
