package ch.usi.inf.bsc.sa4.lab02spring.controller.dto;

import ch.usi.inf.bsc.sa4.lab02spring.model.Level;

public record LevelSummaryDto(
        String id,
        String title,
        String description,
        String creatorName,
        long playCount,
        double clearRate)
{

    
    /// Constructs a LevelSummaryDto from the given Level entity and statistics.
    /// @param level the level to summarize
    /// @param playCount the total number of attempts on this level
    /// @param clearRate the ratio of completed attempts to total attempts
    public LevelSummaryDto(Level level, long playCount, double clearRate) {
        this(
            level.getId(),
            level.getTitle(),
            level.getDescription(),
            level.getCreator().getName(),
            playCount,
            clearRate
        );
    }
}
