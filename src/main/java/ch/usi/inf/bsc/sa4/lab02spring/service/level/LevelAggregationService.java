package ch.usi.inf.bsc.sa4.lab02spring.service.level;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.LevelSummaryDto;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.PublishedLevelSearchCriteria;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.LevelAttitudeType;
import ch.usi.inf.bsc.sa4.lab02spring.repository.AttitudeRepository;
import ch.usi.inf.bsc.sa4.lab02spring.repository.AttemptRepository;
import ch.usi.inf.bsc.sa4.lab02spring.repository.LevelRepository;
import ch.usi.inf.bsc.sa4.lab02spring.service.UserService;
import ch.usi.inf.bsc.sa4.lab02spring.utils.DateRangePreset;
import ch.usi.inf.bsc.sa4.lab02spring.utils.DateRangePreset.RelativeDateRangePreset;
import ch.usi.inf.bsc.sa4.lab02spring.utils.PublishedLevelSearchCriteriaMatcher;
import ch.usi.inf.bsc.sa4.lab02spring.utils.PublishedLevelSortBy;
import ch.usi.inf.bsc.sa4.lab02spring.utils.UserNotFoundException;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/// Builds aggregated views of published levels.
@Service
public class LevelAggregationService {
    /// Loads published levels for aggregation.
    private final LevelRepository levelRepository;
    /// Provides attempt statistics used in summaries.
    private final AttemptRepository attemptRepository;
    /// Provides attitude statistics (likes/dislikes) used in summaries.
    private final AttitudeRepository attitudeRepository;
    /// Resolves users for enriching summaries with user-specific attitudes.
    private final UserService userService;

    /// Creates an aggregation service with repository dependencies.
    ///
    /// @param levelRepository    loads levels to summarize
    /// @param attemptRepository  provides attempt statistics
    /// @param attitudeRepository provides attitude statistics (likes/dislikes)
    @Autowired
    public LevelAggregationService(
            final LevelRepository levelRepository,
            final AttemptRepository attemptRepository,
            final AttitudeRepository attitudeRepository,
            final UserService userService) {
        this.levelRepository = levelRepository;
        this.attemptRepository = attemptRepository;
        this.attitudeRepository = attitudeRepository;
        this.userService = userService;
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
        final String userAttitude = currentUserId == null
                ? null
                : this.attitudeRepository
                        .findByLevelAndUser(level,
                                this.userService.getById(currentUserId).orElseThrow(UserNotFoundException::new))
                        .map(attitude -> attitude.getAttitude().value())
                        .orElse(null);
        final long likeCount = this.attitudeRepository.countByLevelAndAttitude(level, LevelAttitudeType.LIKE);
        final long dislikeCount = this.attitudeRepository.countByLevelAndAttitude(level, LevelAttitudeType.DISLIKE);
        return new LevelSummaryDto(level, playCount, clearRate, popularity, userAttitude, likeCount, dislikeCount);
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
        return getPublishedLevels(sortBy, period, null, currentUserId);
    }

    /// Returns all published levels as summaries, applying the provided search
    /// criteria before sorting.
    ///
    /// @param sortBy        the sorting strategy for published level summaries
    /// @param period        the time range used for popularity; use ALL_TIME to fall
    ///                      back to total play count
    /// @param criteria      optional numeric filters for the result set
    /// @param currentUserId the authenticated user id, or null when unauthenticated
    /// @return a sorted list of LevelSummaryDto for matching published levels
    public List<LevelSummaryDto> getPublishedLevels(
            final PublishedLevelSortBy sortBy,
            final DateRangePreset period,
            final @Nullable PublishedLevelSearchCriteria criteria,
            final @Nullable String currentUserId) {
        final List<LevelSummaryDto> summaries = getPublishedLevelSummaries(sortBy, period, currentUserId);
        final List<LevelSummaryDto> filtered = applySearchCriteria(summaries, criteria);
        return sortPublishedLevels(filtered, sortBy);
    }

    /// Returns a randomly selected summary from the same result set returned by
    /// getPublishedLevels.
    ///
    /// @param sortBy        the sorting strategy for published level summaries
    /// @param period        the time range used for popularity; use ALL_TIME to fall
    ///                      back to total play count
    /// @param criteria      optional numeric filters for the result set
    /// @param currentUserId the authenticated user id, or null when unauthenticated
    /// @return a random matching summary, or empty when no levels match
    public Optional<LevelSummaryDto> getRandomPublishedLevel(
            final PublishedLevelSortBy sortBy,
            final DateRangePreset period,
            final @Nullable PublishedLevelSearchCriteria criteria,
            final @Nullable String currentUserId) {
        final List<LevelSummaryDto> results = getPublishedLevels(sortBy, period, criteria, currentUserId);
        if (results.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(results.get(ThreadLocalRandom.current().nextInt(results.size())));
    }

    private List<LevelSummaryDto> getPublishedLevelSummaries(
            final PublishedLevelSortBy sortBy,
            final DateRangePreset period,
            final @Nullable String currentUserId) {
        final List<Level> levels = this.levelRepository.findByPublishedTrue();

        return levels.stream()
                .map(level -> toLevelSummary(level, sortBy, period, currentUserId))
                .toList();
    }

    private List<LevelSummaryDto> applySearchCriteria(
            final List<LevelSummaryDto> summaries,
            final @Nullable PublishedLevelSearchCriteria criteria) {
        return summaries.stream()
                .filter(summary -> PublishedLevelSearchCriteriaMatcher.matches(summary, criteria))
                .toList();
    }

    private static List<LevelSummaryDto> sortPublishedLevels(
            final List<LevelSummaryDto> summaries,
            final PublishedLevelSortBy sortBy) {
        if (sortBy == PublishedLevelSortBy.CLEAR_RATE) {
            return summaries.stream()
                    .sorted(Comparator.comparingDouble(LevelSummaryDto::clearRate).reversed())
                    .toList();
        } else if (sortBy == PublishedLevelSortBy.POPULARITY) {
            return summaries.stream()
                    .sorted(Comparator.comparingLong(LevelSummaryDto::popularity).reversed())
                    .toList();
        } else {
            throw new IllegalStateException("Unsupported published level sort: " + sortBy);
        }
    }
}
