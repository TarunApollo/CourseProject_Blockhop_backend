package ch.usi.inf.bsc.sa4.lab02spring.utils;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.LevelSummaryDto;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.PublishedLevelSearchCriteria;

import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/// Unit tests for [PublishedLevelSearchCriteriaMatcher].
@DisplayName("The PublishedLevelSearchCriteriaMatcher")
/* package */ class PublishedLevelSearchCriteriaMatcherTests {
    /// Representative summary used by the matcher tests.
    private static final LevelSummaryDto SUMMARY = new LevelSummaryDto(
            "level-1",
            "title",
            "description",
            "creator",
            10,
            0.5,
            20,
            null,
            4,
            2,
            Map.of(),
            Map.of());

    /// Null criteria should not filter out any summary.
    @Test
    @DisplayName("matches when criteria is null")
    void matchesNullCriteria() {
        Assertions.assertTrue(PublishedLevelSearchCriteriaMatcher.matches(SUMMARY, null));
    }

    /// Tests for clear-rate bounds.
    @Nested
    @DisplayName("when clear-rate bounds are present")
    class ClearRateBounds {
        /// Boundary values are inclusive.
        @Test
        @DisplayName("matches inclusive clear-rate bounds")
        void matchesInclusiveBounds() {
            final PublishedLevelSearchCriteria criteria = new PublishedLevelSearchCriteria(
                    0.5, 0.5, null, null, null, null, null, null);

            Assertions.assertTrue(PublishedLevelSearchCriteriaMatcher.matches(SUMMARY, criteria));
        }

        /// Values outside the configured range should be rejected.
        @Test
        @DisplayName("rejects clear rates outside the bounds")
        void rejectsOutsideBounds() {
            final PublishedLevelSearchCriteria criteria = new PublishedLevelSearchCriteria(
                    0.6, null, null, null, null, null, null, null);

            Assertions.assertFalse(PublishedLevelSearchCriteriaMatcher.matches(SUMMARY, criteria));
        }
    }

    /// Tests for attempt count bounds.
    @Nested
    @DisplayName("when attempt bounds are present")
    class AttemptBounds {
        /// The matcher should compare attempt bounds against playCount.
        @Test
        @DisplayName("matches play count inside the attempt bounds")
        void matchesInsideBounds() {
            final PublishedLevelSearchCriteria criteria = new PublishedLevelSearchCriteria(
                    null, null, 5L, 10L, null, null, null, null);

            Assertions.assertTrue(PublishedLevelSearchCriteriaMatcher.matches(SUMMARY, criteria));
        }

        /// The matcher should reject summaries below the minimum attempt count.
        @Test
        @DisplayName("rejects play count outside the attempt bounds")
        void rejectsOutsideBounds() {
            final PublishedLevelSearchCriteria criteria = new PublishedLevelSearchCriteria(
                    null, null, 11L, null, null, null, null, null);

            Assertions.assertFalse(PublishedLevelSearchCriteriaMatcher.matches(SUMMARY, criteria));
        }
    }

    /// Tests for attitude count bounds.
    @Nested
    @DisplayName("when attitude bounds are present")
    class AttitudeBounds {
        /// Like and dislike bounds should both be enforced.
        @Test
        @DisplayName("matches like and dislike counts inside the bounds")
        void matchesInsideBounds() {
            final PublishedLevelSearchCriteria criteria = new PublishedLevelSearchCriteria(
                    null, null, null, null, 3L, 5L, 1L, 3L);

            Assertions.assertTrue(PublishedLevelSearchCriteriaMatcher.matches(SUMMARY, criteria));
        }

        /// A summary should fail when any configured bound fails.
        @Test
        @DisplayName("rejects when any attitude bound fails")
        void rejectsWhenAnyBoundFails() {
            final PublishedLevelSearchCriteria criteria = new PublishedLevelSearchCriteria(
                    null, null, null, null, 5L, null, null, 3L);

            Assertions.assertFalse(PublishedLevelSearchCriteriaMatcher.matches(SUMMARY, criteria));
        }
    }
}
