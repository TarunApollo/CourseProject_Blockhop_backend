package ch.usi.inf.bsc.sa4.lab02spring.controller.dto;

import ch.usi.inf.bsc.sa4.lab02spring.model.*;

import java.util.Map;

import ch.usi.inf.bsc.sa4.lab02spring.model.Position;
import ch.usi.inf.bsc.sa4.lab02spring.utils.FieldSerializer;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

public record LevelDTO(
        String id,
        String title,
        String description,
        User creator,
        boolean published,
        boolean publishEligible,
        ClearCondition clearCondition,
        @JsonSerialize(using = FieldSerializer.LevelDTOObjectLayerSerializer.class)
        @JsonDeserialize(keyUsing = FieldSerializer.PositionKeyDeserializer.class)
        Map<Position, GameObject> objectLayer,
        @JsonSerialize(using = FieldSerializer.LevelDTOWorldLayerSerializer.class)
        @JsonDeserialize(keyUsing = FieldSerializer.PositionKeyDeserializer.class)
        Map<Position, GroundObject> worldLayer
) {

    /// Constructs a LevelDTO from the given Level entity.
    /// @param level the level to convert into a DTO
    public LevelDTO(final Level level) {
        this(
                level.getId(),
                level.getTitle(),
                level.getDescription(),
                level.getCreator(),
                level.isPublished(),
                level.isPublishEligible(),
                level.getClearCondition(),
                level.getObjectLayer(),
                level.getWorldLayer()
        );
    }
}