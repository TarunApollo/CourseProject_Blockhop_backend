package ch.usi.inf.bsc.sa4.lab02spring.controller.dto;

import ch.usi.inf.bsc.sa4.lab02spring.model.GroundObject;
import ch.usi.inf.bsc.sa4.lab02spring.model.Position;
import ch.usi.inf.bsc.sa4.lab02spring.utils.FieldSerializer;
import tools.jackson.databind.annotation.JsonDeserialize;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Map;

public record AttemptDTO(
//        @JsonSerialize(using = FieldSerializer.LevelDTOObjectLayerSerializer.class)
//        Map<Position, GameObject> objectLayer,
        @JsonDeserialize(using = FieldSerializer.WorldLayerDeserializer.class)
        Map<Position, GroundObject> worldLayer,
        Position playerPosition,
        ZonedDateTime timestamp,
        Duration timeTaken,
        boolean completed
) {
}
