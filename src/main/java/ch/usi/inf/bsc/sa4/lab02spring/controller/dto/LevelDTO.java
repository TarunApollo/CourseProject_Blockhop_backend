package ch.usi.inf.bsc.sa4.lab02spring.controller.dto;

import ch.usi.inf.bsc.sa4.lab02spring.model.*;
import java.util.Map;
import ch.usi.inf.bsc.sa4.lab02spring.model.Position;

public record LevelDTO(
    String id,
    String title,
    String description,
    User creator,
    boolean published,
    Map<Position, GameObject> objectLayer
) {

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