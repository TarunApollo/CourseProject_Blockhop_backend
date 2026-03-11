package ch.usi.inf.bsc.sa4.lab02spring.controller.dto;

import ch.usi.inf.bsc.sa4.lab02spring.model.Coin;
import ch.usi.inf.bsc.sa4.lab02spring.model.GameObject;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.Position;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.databind.annotation.JsonSerialize;

import java.util.List;
import java.util.Map;

public record LevelDTO(
        String id,
        String title,
        String description,
        String creatorId,
        boolean published,
        @JsonSerialize(using=LevelDTOObjectLayerSerializer.class)
        Map<Position, GameObject> objectLayer
) {

    public LevelDTO(Level level) {
        this(
            level.getId(),
            level.getTitle(),
            level.getDescription(),
            level.getCreatorId(),
            level.isPublished(),
                level.getObjectLayer()
        );
    }
}