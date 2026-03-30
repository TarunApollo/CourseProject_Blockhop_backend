package ch.usi.inf.bsc.sa4.lab02spring.controller.dto;

import ch.usi.inf.bsc.sa4.lab02spring.model.GameObject;
import ch.usi.inf.bsc.sa4.lab02spring.model.GroundObject;
import ch.usi.inf.bsc.sa4.lab02spring.model.Position;
import ch.usi.inf.bsc.sa4.lab02spring.utils.FieldSerializer;
import tools.jackson.databind.annotation.JsonSerialize;

import java.util.Map;

public record ValidateLevelDTO(
//        @JsonSerialize(using = FieldSerializer.LevelDTOObjectLayerSerializer.class)
//        Map<Position, GameObject> objectLayer,
        @JsonSerialize(using = FieldSerializer.LevelDTOWorldLayerSerializer.class)
        Map<Position, GroundObject> worldLayer,
        Position playerPosition
) {
}
