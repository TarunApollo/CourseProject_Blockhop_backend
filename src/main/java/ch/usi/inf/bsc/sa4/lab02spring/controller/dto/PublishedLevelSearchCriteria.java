package ch.usi.inf.bsc.sa4.lab02spring.controller.dto;

import org.jspecify.annotations.Nullable;

/// Optional numeric filters for published level search results.
public record PublishedLevelSearchCriteria(
        @Nullable Double minClearRate,
        @Nullable Double maxClearRate,
        @Nullable Long minAttempts,
        @Nullable Long maxAttempts,
        @Nullable Long minLikes,
        @Nullable Long maxLikes,
        @Nullable Long minDislikes,
        @Nullable Long maxDislikes
) {
}
