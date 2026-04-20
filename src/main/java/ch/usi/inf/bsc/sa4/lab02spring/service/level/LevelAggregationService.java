package ch.usi.inf.bsc.sa4.lab02spring.service.level;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.LevelSummaryDto;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.repository.AttemptRepository;
import ch.usi.inf.bsc.sa4.lab02spring.repository.LevelRepository;
import ch.usi.inf.bsc.sa4.lab02spring.utils.DateRangePreset;
import ch.usi.inf.bsc.sa4.lab02spring.utils.DateRangePreset.RelativeDateRangePreset;
import ch.usi.inf.bsc.sa4.lab02spring.utils.PublishedLevelSortBy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class LevelAggregationService {
    private final LevelRepository levelRepository;
    private final AttemptRepository attemptRepository;

    @Autowired
    public LevelAggregationService(
            LevelRepository levelRepository,
            AttemptRepository attemptRepository) {
        this.levelRepository = levelRepository;
        this.attemptRepository = attemptRepository;
    }

    /// Builds a LevelSummaryDto for the given level, computing play count, clear rate, and popularity.
    /// @param level the level to summarize
    /// @param period the time range used to compute popularity
    /// @return a LevelSummaryDto with computed statistics
    private LevelSummaryDto toLevelSummary(Level level, PublishedLevelSortBy sortBy, DateRangePreset period) {
        long playCount = this.attemptRepository.countByLevel(level);
        long clearCount = this.attemptRepository.countByLevelAndCompletedTrue(level);
        double clearRate = playCount == 0 ? 0 : (double) clearCount / playCount;
        long popularity = playCount;
        if (sortBy == PublishedLevelSortBy.POPULARITY && period instanceof RelativeDateRangePreset relative) {
            popularity = this.attemptRepository.countByLevelAndTimestampAfter(level, relative.rangeStart());
        }

        return new LevelSummaryDto(level, playCount, clearRate, popularity);
    }

    /// Returns all published levels as summaries, sorted by the given criteria.
    /// @param sortBy the sorting strategy for published level summaries
    /// @param period the time range used to compute popularity; use ALL_TIME to fall back to total play count
    /// @return a sorted list of LevelSummaryDto for all published levels
    public List<LevelSummaryDto> getPublishedLevels(PublishedLevelSortBy sortBy, DateRangePreset period) {
        List<Level> levels = this.levelRepository.findByPublishedTrue();

        List<LevelSummaryDto> dtos = levels.stream()
                .map(level -> toLevelSummary(level, sortBy, period))
                .toList();

        return switch (sortBy) {
            case CLEAR_RATE -> dtos.stream()
                    .sorted(Comparator.comparingDouble(LevelSummaryDto::clearRate).reversed())
                    .toList();
            case POPULARITY -> dtos.stream()
                    .sorted(Comparator.comparingLong(LevelSummaryDto::popularity).reversed())
                    .toList();
        };
    }
}
