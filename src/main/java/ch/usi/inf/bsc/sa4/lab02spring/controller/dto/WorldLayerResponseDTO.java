package ch.usi.inf.bsc.sa4.lab02spring.controller.dto;

import java.util.Map;

import ch.usi.inf.bsc.sa4.lab02spring.model.GroundObject;
import ch.usi.inf.bsc.sa4.lab02spring.model.Position;
import ch.usi.inf.bsc.sa4.lab02spring.utils.FieldSerializer;
import tools.jackson.databind.annotation.JsonSerialize;

//TODO: docs
public record WorldLayerResponseDTO(
    String levelId,
    @JsonSerialize(using = FieldSerializer.LevelDTOWorldLayerSerializer.class)
    Map<Position, GroundObject> worldLayer
) {

}