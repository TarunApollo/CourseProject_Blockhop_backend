package ch.usi.inf.bsc.sa4.lab02spring.utils;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.LevelSummaryDto;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.PublishedLevelSearchCriteria;
import org.jspecify.annotations.Nullable;

/// Matches published level summaries against optional search criteria.
public final class PublishedLevelSearchCriteriaMatcher {
    private PublishedLevelSearchCriteriaMatcher() {
    }

    /// Returns true when the summary satisfies every non-null search bound.
    ///
    /// @param summary  the level summary to test
    /// @param criteria optional search criteria; null means no filtering
    /// @return true when the summary should remain in the result set
    public static boolean matches(
            final LevelSummaryDto summary,
            final @Nullable PublishedLevelSearchCriteria criteria) {
        return criteria == null || matchesAllBounds(summary, criteria);
    }

    private static boolean matchesAllBounds(
            final LevelSummaryDto summary,
            final PublishedLevelSearchCriteria criteria) {
        return matchesMinMax(summary.clearRate(), criteria.minClearRate(), criteria.maxClearRate())
                && matchesMinMax(summary.playCount(), criteria.minAttempts(), criteria.maxAttempts())
                && matchesMinMax(summary.likeCount(), criteria.minLikes(), criteria.maxLikes())
                && matchesMinMax(summary.dislikeCount(), criteria.minDislikes(), criteria.maxDislikes());
    }

    private static boolean matchesMinMax(
            final double value,
            final @Nullable Double min,
            final @Nullable Double max) {
        return (min == null || value >= min) && (max == null || value <= max);
    }

    private static boolean matchesMinMax(
            final long value,
            final @Nullable Long min,
            final @Nullable Long max) {
        return (min == null || value >= min) && (max == null || value <= max);
    }
}
