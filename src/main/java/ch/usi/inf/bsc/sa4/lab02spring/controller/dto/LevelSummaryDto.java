package ch.usi.inf.bsc.sa4.lab02spring.controller.dto;

import ch.usi.inf.bsc.sa4.lab02spring.model.GameObject;
import ch.usi.inf.bsc.sa4.lab02spring.model.GroundObject;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.Position;
import ch.usi.inf.bsc.sa4.lab02spring.utils.FieldSerializer;
import tools.jackson.databind.annotation.JsonSerialize;

import java.util.Map;

public record LevelSummaryDto(
        String id,
        String title,
        String description,
        String creatorName,
        long playCount,
        double clearRate,
        long popularity,
        @JsonSerialize(using = FieldSerializer.LevelDTOObjectLayerSerializer.class)
        Map<Position, GameObject> objectLayer,
        @JsonSerialize(using = FieldSerializer.LevelDTOWorldLayerSerializer.class)
        Map<Position, GroundObject> worldLayer) {

    /// Constructs a LevelSummaryDto from the given Level entity and statistics.
    /// 
    /// @param level     the level to summarize
    /// @param playCount the total number of attempts on this level
    /// @param clearRate the ratio of completed attempts to total attempts
    /// @param popularity the number of attempts within a recent time period
    public LevelSummaryDto(final Level level, 
        final long playCount, 
        final double clearRate, 
        final long popularity) {
        this(
                level.getId(),
                level.getTitle(),
                level.getDescription(),
                level.getCreator().getName(),
                playCount,
                clearRate,
                popularity,
                level.getObjectLayer(),
                level.getWorldLayer());
    }
}
