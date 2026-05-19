package ch.usi.inf.bsc.sa4.lab02spring.controller.dto;

import ch.usi.inf.bsc.sa4.lab02spring.model.ClearCondition;
import ch.usi.inf.bsc.sa4.lab02spring.model.GameObject;
import ch.usi.inf.bsc.sa4.lab02spring.model.GroundObject;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.Position;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.utils.FieldSerializer;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.Map;

@SuppressFBWarnings(
        value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2", "US_USELESS_SUPPRESSION_ON_CLASS"},
        justification = "Transient response DTO; mutability is intentional and not shared concurrently")
public record CreatedLevelProfileDTO(
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
        Map<Position, GroundObject> worldLayer,
        long playCount,
        long completeCount
) {
    /// Convenience constructor that derives layer/condition fields from a Level.
    /// @param level         the level whose fields populate the DTO
    /// @param playCount     non-creator play attempts on the level
    /// @param completeCount non-creator successful completions
    public CreatedLevelProfileDTO(final Level level, final long playCount, final long completeCount) {
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
