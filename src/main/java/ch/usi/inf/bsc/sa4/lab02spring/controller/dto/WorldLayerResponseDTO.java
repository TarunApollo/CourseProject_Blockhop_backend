package ch.usi.inf.bsc.sa4.lab02spring.controller.dto;

import ch.usi.inf.bsc.sa4.lab02spring.model.GroundObject;
import ch.usi.inf.bsc.sa4.lab02spring.model.Position;
import ch.usi.inf.bsc.sa4.lab02spring.utils.FieldSerializer;
import tools.jackson.databind.annotation.JsonSerialize;

import java.util.Map;

//TODO: docs
record WorldLayerResponseDTO(
    String levelId,
    @JsonSerialize(using = FieldSerializer.LevelDTOWorldLayerSerializer.class)
    Map<Position, GroundObject> worldLayer
) {

    public WorldLayerResponseDTO(String levelId, Map<Position, GroundObject> worldLayer) {
        this.levelId = levelId;
        this.worldLayer = worldLayer;
    }
}