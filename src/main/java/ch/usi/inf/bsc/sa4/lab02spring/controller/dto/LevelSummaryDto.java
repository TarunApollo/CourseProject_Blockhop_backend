package ch.usi.inf.bsc.sa4.lab02spring.controller.dto;

import ch.usi.inf.bsc.sa4.lab02spring.model.GameObject;
import ch.usi.inf.bsc.sa4.lab02spring.model.GroundObject;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.Position;
import ch.usi.inf.bsc.sa4.lab02spring.utils.FieldSerializer;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.jspecify.annotations.Nullable;

import java.util.Map;

@SuppressFBWarnings(value = { "EI_EXPOSE_REP", "EI_EXPOSE_REP2",
                "US_USELESS_SUPPRESSION_ON_CLASS" }, justification = "Transient response DTO; mutability is intentional and not shared concurrently")
public record LevelSummaryDto(
                String id,
                String title,
                String description,
                String creatorName,
                long playCount,
                double clearRate,
                long popularity,
                @Nullable String userAttitude,
                @JsonSerialize(using = FieldSerializer.LevelDTOObjectLayerSerializer.class) @JsonDeserialize(keyUsing = FieldSerializer.PositionKeyDeserializer.class) Map<Position, GameObject> objectLayer,
                @JsonSerialize(using = FieldSerializer.LevelDTOWorldLayerSerializer.class) @JsonDeserialize(keyUsing = FieldSerializer.PositionKeyDeserializer.class) Map<Position, GroundObject> worldLayer) {

        /// Constructs a LevelSummaryDto from the given Level entity and statistics.
        /// 
        /// @param level      the level to summarize
        /// @param playCount  the total number of attempts on this level
        /// @param clearRate  the ratio of completed attempts to total attempts
        /// @param popularity the number of attempts within a recent time period
        public LevelSummaryDto(final Level level,
                        final long playCount,
                        final double clearRate,
                        final long popularity) {
                this(level, playCount, clearRate, popularity, null);
        }

        /// Constructs a LevelSummaryDto from the given Level entity and statistics,
        /// enriched with the current user's attitude if available.
        ///
        /// @param level        the level to summarize
        /// @param playCount    the total number of attempts on this level
        /// @param clearRate    the ratio of completed attempts to total attempts
        /// @param popularity   the number of attempts within a recent time period
        /// @param userAttitude the current user's attitude for this level, or null
        public LevelSummaryDto(final Level level,
                        final long playCount,
                        final double clearRate,
                        final long popularity,
                        final @Nullable String userAttitude) {
                this(
                                level.getId(),
                                level.getTitle(),
                                level.getDescription(),
                                level.getCreator().getName(),
                                playCount,
                                clearRate,
                                popularity,
                                userAttitude,
                                level.getObjectLayer(),
                                level.getWorldLayer());
        }
}
