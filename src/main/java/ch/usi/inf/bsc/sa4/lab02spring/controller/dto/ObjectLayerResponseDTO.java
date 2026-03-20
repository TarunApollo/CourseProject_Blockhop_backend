package ch.usi.inf.bsc.sa4.lab02spring.controller.dto;

import java.util.Map;

import ch.usi.inf.bsc.sa4.lab02spring.model.GameObject;
import ch.usi.inf.bsc.sa4.lab02spring.model.Position;
import ch.usi.inf.bsc.sa4.lab02spring.utils.FieldSerializer;
import tools.jackson.databind.annotation.JsonSerialize;

//TODO: docs
public record ObjectLayerResponseDTO(
    String levelId,
    @JsonSerialize(using = FieldSerializer.LevelDTOObjectLayerSerializer.class)
    Map<Position, GameObject> worldLayer
) {

    public ObjectLayerResponseDTO(String levelId, Map<Position, GameObject> worldLayer) {
        this.levelId = levelId;
        this.worldLayer = worldLayer;
    }
}