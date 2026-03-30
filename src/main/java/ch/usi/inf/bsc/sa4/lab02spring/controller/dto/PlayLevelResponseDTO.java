package ch.usi.inf.bsc.sa4.lab02spring.controller.dto;

import ch.usi.inf.bsc.sa4.lab02spring.model.*;

import java.util.Map;

import ch.usi.inf.bsc.sa4.lab02spring.model.Position;
import ch.usi.inf.bsc.sa4.lab02spring.utils.FieldSerializer;
import tools.jackson.databind.annotation.JsonSerialize;

/**
 * DTO returned when a player enters a level through the play flow.
 * Contains everything the level player needs to render and enforce the level,
 * but excludes editor-only or sensitive information.
 */
public record PlayLevelResponseDTO(
    String id,
    String title,
    String description,
    int width,
    int height,
    ClearCondition clearCondition,
    @JsonSerialize(using = FieldSerializer.LevelDTOWorldLayerSerializer.class)
    Map<Position, GroundObject> worldLayer,
    @JsonSerialize(using = FieldSerializer.LevelDTOObjectLayerSerializer.class)
    Map<Position, GameObject> objectLayer
) {

    /// Constructs a PlayLevelResponseDTO from the given Level entity.
    /// @param level the published level to convert
    public PlayLevelResponseDTO(Level level) {
        this(
            level.getId(),
            level.getTitle(),
            level.getDescription(),
            level.getWidth(),
            level.getHeight(),
            level.getClearCondition(),
            level.getWorldLayer(),
            level.getObjectLayer()
        );
    }
}
