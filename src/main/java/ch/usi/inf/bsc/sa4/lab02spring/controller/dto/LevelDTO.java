package ch.usi.inf.bsc.sa4.lab02spring.controller.dto;

import ch.usi.inf.bsc.sa4.lab02spring.model.*;
import java.util.Map;
import ch.usi.inf.bsc.sa4.lab02spring.model.Position;
import tools.jackson.databind.annotation.JsonSerialize;

public record LevelDTO(
        String id,
        String title,
        String description,
        User creator,
        boolean published,
        @JsonSerialize(using=LevelDTOObjectLayerSerializer.class)
        Map<Position, GameObject> objectLayer
) {

    /// Constructs a LevelDTO from the given Level entity.
    /// @param level the level to convert into a DTO
    public LevelDTO(Level level) {
        this(
            level.getId(),
            level.getTitle(),
            level.getDescription(),
            level.getCreator(),
            level.isPublished(),
                level.getObjectLayer()
        );
    }
}