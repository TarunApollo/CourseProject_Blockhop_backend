package ch.usi.inf.bsc.sa4.lab02spring.controller.dto;

import ch.usi.inf.bsc.sa4.lab02spring.model.*;
import ch.usi.inf.bsc.sa4.lab02spring.utils.FieldSerializer;
import tools.jackson.databind.annotation.JsonSerialize;

import java.util.Map;

public record CreatedLevelProfileDTO(
        String id,
        String title,
        String description,
        User creator,
        boolean published,
        boolean publishEligible,
        ClearCondition clearCondition,
        @JsonSerialize(using = FieldSerializer.LevelDTOObjectLayerSerializer.class)
        Map<Position, GameObject> objectLayer,
        @JsonSerialize(using = FieldSerializer.LevelDTOWorldLayerSerializer.class)
        Map<Position, GroundObject> worldLayer,
        long playCount,
        long completeCount
) {
    public CreatedLevelProfileDTO(Level level, long playCount, long completeCount) {
        this(
                level.getId(),
                level.getTitle(),
                level.getDescription(),
                level.getCreator(),
                level.isPublished(),
                level.isPublishEligible(),
                level.getClearCondition(),
                level.getObjectLayer(),
                level.getWorldLayer(),
                playCount,
                completeCount
        );
    }
}
