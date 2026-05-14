package ch.usi.inf.bsc.sa4.lab02spring.service.level;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.LevelSummaryDto;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.repository.AttitudeRepository;
import ch.usi.inf.bsc.sa4.lab02spring.repository.AttemptRepository;
import ch.usi.inf.bsc.sa4.lab02spring.repository.LevelRepository;
import ch.usi.inf.bsc.sa4.lab02spring.utils.DateRangePreset;
import ch.usi.inf.bsc.sa4.lab02spring.utils.DateRangePreset.RelativeDateRangePreset;
import ch.usi.inf.bsc.sa4.lab02spring.utils.PublishedLevelSortBy;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/// Builds aggregated views of published levels.
@Service
public class LevelAggregationService {
    /// Loads published levels for aggregation.
    private final LevelRepository levelRepository;
    /// Provides attempt statistics used in summaries.
    private final AttemptRepository attemptRepository;
    /// Provides attitude statistics (likes/dislikes) used in summaries.
    private final AttitudeRepository attitudeRepository;

    /// Creates an aggregation service with repository dependencies.
    ///
    /// @param levelRepository    loads levels to summarize
    /// @param attemptRepository  provides attempt statistics
    /// @param attitudeRepository provides attitude statistics (likes/dislikes)
    @Autowired
    public LevelAggregationService(
            final LevelRepository levelRepository,
            final AttemptRepository attemptRepository,
            final AttitudeRepository attitudeRepository) {
        this.levelRepository = levelRepository;
        this.attemptRepository = attemptRepository;
        this.attitudeRepository = attitudeRepository;
    }

    /// Builds a summary for the given level. Computes play count, clear rate,
    /// popularity, likes, and dislikes.
    /// 
    /// @param level  the level to summarize
    /// @param period the time range used to compute popularity
    /// @return a LevelSummaryDto with computed statistics
    private LevelSummaryDto toLevelSummary(
            final Level level,
            final PublishedLevelSortBy sortBy,
            final DateRangePreset period,
            final @Nullable String currentUserId) {
        final long playCount = this.attemptRepository.countByLevel(level);
        final long clearCount = this.attemptRepository.countByLevelAndCompletedTrue(level);
        final double clearRate = playCount == 0 ? 0 : (double) clearCount / playCount;
        long popularity = playCount;
        if (sortBy == PublishedLevelSortBy.POPULARITY && period instanceof RelativeDateRangePreset relative) {
            popularity = this.attemptRepository.countByLevelAndTimestampAfter(level, relative.rangeStart());
        }
        final long likes = this.attitudeRepository.countLikesByLevel(level);
        final long dislikes = this.attitudeRepository.countDislikesByLevel(level);
        final String userAttitude = currentUserId == null
                ? null
                : this.attitudeRepository.findByLevelIdAndUserId(level.getId(), currentUserId)
                        .map(attitude -> attitude.getAttitude().value())
                        .orElse(null);

        return new LevelSummaryDto(level, playCount, clearRate, popularity, likes, dislikes, userAttitude);
    }

    /// Returns all published levels as summaries. Applies the requested sorting
    /// strategy.
    /// 
    /// @param sortBy the sorting strategy for published level summaries
    /// @param period the time range used for popularity; use ALL_TIME to fall back
    ///               to total play count
    /// @return a sorted list of LevelSummaryDto for all published levels
    public List<LevelSummaryDto> getPublishedLevels(
            final PublishedLevelSortBy sortBy,
            final DateRangePreset period) {
        return getPublishedLevels(sortBy, period, null);
    }

    /// Returns all published levels as summaries and enriches each summary with the
    /// provided user's attitude when available.
    ///
    /// @param sortBy        the sorting strategy for published level summaries
    /// @param period        the time range used for popularity; use ALL_TIME to fall
    ///                      back to total play count
    /// @param currentUserId the authenticated user id, or null when unauthenticated
    /// @return a sorted list of LevelSummaryDto for all published levels
    public List<LevelSummaryDto> getPublishedLevels(
            final PublishedLevelSortBy sortBy,
            final DateRangePreset period,
            final @Nullable String currentUserId) {
        final List<Level> levels = this.levelRepository.findByPublishedTrue();

        final List<LevelSummaryDto> dtos = levels.stream()
                .map(level -> toLevelSummary(level, sortBy, period, currentUserId))
                .toList();

        final List<LevelSummaryDto> result;
        if (sortBy == PublishedLevelSortBy.CLEAR_RATE) {
            result = dtos.stream()
                    .sorted(Comparator.comparingDouble(LevelSummaryDto::clearRate).reversed())
                    .toList();
        } else if (sortBy == PublishedLevelSortBy.POPULARITY) {
            result = dtos.stream()
                    .sorted(Comparator.comparingLong(LevelSummaryDto::popularity).reversed())
                    .toList();
        } else {
            throw new IllegalStateException("Unsupported published level sort: " + sortBy);
        }
        return result;
    }
}
