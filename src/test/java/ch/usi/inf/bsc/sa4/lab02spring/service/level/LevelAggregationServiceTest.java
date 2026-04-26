package ch.usi.inf.bsc.sa4.lab02spring.service.level;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.LevelSummaryDto;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.repository.AttemptRepository;
import ch.usi.inf.bsc.sa4.lab02spring.repository.LevelRepository;
import ch.usi.inf.bsc.sa4.lab02spring.utils.DateRangePreset;
import ch.usi.inf.bsc.sa4.lab02spring.utils.PublishedLevelSortBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/// Unit tests for the published-levels aggregation service.
/// Verifies sort strategies, popularity windows, and clear-rate computation.
@DisplayName("LevelAggregationService.getPublishedLevels")
@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"NullAway", "PMD.TooManyStaticImports"})
class LevelAggregationServiceTest {

    /// Default level description used in fixtures.
    private static final String DESC = "desc";
    /// Default creator display name used in fixtures.
    private static final String CREATOR_NAME = "Mario";
    /// Default creator id used in fixtures.
    private static final String CREATOR_ID = "user-1";
    /// Sample level title used by metadata tests.
    private static final String SAMPLE_TITLE = "My Title";

    /// Mocked level repository providing per-test fixtures.
    @Mock private LevelRepository levelRepository;
    /// Mocked attempt repository providing aggregation counts.
    @Mock private AttemptRepository attemptRepository;

    /// Service under test, with mocks injected.
    @InjectMocks private LevelAggregationService service;

    /// Shared creator used as the owner of fixture levels.
    private User creator;

    /// Initializes the level creator used by all tests.
    @BeforeEach
    void setUp() {
        creator = new User(CREATOR_ID, CREATOR_NAME);
    }

    /// Builds a published-style level owned by the shared creator.
    private Level publishedLevel(final String title) {
        return new Level(title, DESC, creator);
    }

    /// Verifies that an empty repository result yields an empty summary list.
    @Test
    @DisplayName("returns an empty list when no published levels exist")
    void emptyWhenNoLevels() {
        when(levelRepository.findByPublishedTrue()).thenReturn(List.of());

        final List<LevelSummaryDto> result =
            service.getPublishedLevels(PublishedLevelSortBy.CLEAR_RATE,
                DateRangePreset.AllTimeDateRangePreset.ALL_TIME);

        assertEquals(List.of(), result);
    }

    // --------------------------------------------------------------------
    // CLEAR_RATE sorting
    // --------------------------------------------------------------------

    /// Tests for clear-rate sort behavior.
    @Nested
    @DisplayName("when sortBy is CLEAR_RATE")
    class ClearRateSorting {

        /// Three levels with different clear ratios should be ordered descending.
        @Test
        @DisplayName("sorts levels by clear rate in descending order")
        void sortsDescending() {
            final Level a = publishedLevel("low-rate");
            final Level b = publishedLevel("high-rate");
            final Level c = publishedLevel("mid-rate");
            when(levelRepository.findByPublishedTrue()).thenReturn(List.of(a, b, c));
            // a: 1/4 = 0.25, b: 9/10 = 0.9, c: 5/10 = 0.5
            stubAttempts(a, 4, 1);
            stubAttempts(b, 10, 9);
            stubAttempts(c, 10, 5);

            final List<LevelSummaryDto> result = service.getPublishedLevels(
                PublishedLevelSortBy.CLEAR_RATE,
                DateRangePreset.AllTimeDateRangePreset.ALL_TIME);

            assertEquals(List.of("high-rate", "mid-rate", "low-rate"),
                result.stream().map(LevelSummaryDto::title).toList());
        }

        /// playCount=0 should yield clearRate=0 to avoid a division by zero.
        @Test
        @DisplayName("computes clearRate as 0 when playCount is 0 to avoid division by zero")
        void clearRateZeroWhenNoPlays() {
            final Level a = publishedLevel("never-played");
            when(levelRepository.findByPublishedTrue()).thenReturn(List.of(a));
            stubAttempts(a, 0, 0);

            final List<LevelSummaryDto> result = service.getPublishedLevels(
                PublishedLevelSortBy.CLEAR_RATE,
                DateRangePreset.AllTimeDateRangePreset.ALL_TIME);

            assertEquals(0.0, result.get(0).clearRate());
        }

        /// 2 of 4 attempts completed should yield clearRate=0.5.
        @Test
        @DisplayName("computes clearRate as the ratio of completed attempts to total attempts")
        void clearRateIsCorrectRatio() {
            final Level a = publishedLevel("half-cleared");
            when(levelRepository.findByPublishedTrue()).thenReturn(List.of(a));
            stubAttempts(a, 4, 2);

            final List<LevelSummaryDto> result = service.getPublishedLevels(
                PublishedLevelSortBy.CLEAR_RATE,
                DateRangePreset.AllTimeDateRangePreset.ALL_TIME);

            assertEquals(0.5, result.get(0).clearRate());
        }

        /// Even with a relative window, the time-window query must not be used.
        @Test
        @DisplayName("never queries the time-window repository when sorting by clear rate")
        void doesNotQueryTimeWindow() {
            final Level a = publishedLevel("a");
            when(levelRepository.findByPublishedTrue()).thenReturn(List.of(a));
            stubAttempts(a, 5, 3);

            service.getPublishedLevels(
                PublishedLevelSortBy.CLEAR_RATE,
                DateRangePreset.RelativeDateRangePreset.LAST_7_DAYS);

            verify(attemptRepository, never()).countByLevelAndTimestampAfter(any(), any());
        }

        /// When sorting by clear rate, popularity should fall back to total play count.
        @Test
        @DisplayName("populates popularity with the total play count")
        void popularityFallsBackToPlayCount() {
            final Level a = publishedLevel("a");
            when(levelRepository.findByPublishedTrue()).thenReturn(List.of(a));
            stubAttempts(a, 7, 3);

            final List<LevelSummaryDto> result = service.getPublishedLevels(
                PublishedLevelSortBy.CLEAR_RATE,
                DateRangePreset.AllTimeDateRangePreset.ALL_TIME);

            assertEquals(7L, result.get(0).popularity());
        }
    }

    // --------------------------------------------------------------------
    // POPULARITY sorting
    // --------------------------------------------------------------------

    /// Tests for popularity sort behavior, including the time-window branch.
    @Nested
    @DisplayName("when sortBy is POPULARITY")
    class PopularitySorting {

        /// Three levels with different popularity values should be ordered descending.
        @Test
        @DisplayName("sorts levels by popularity in descending order")
        void sortsDescending() {
            final Level a = publishedLevel("low-pop");
            final Level b = publishedLevel("high-pop");
            final Level c = publishedLevel("mid-pop");
            when(levelRepository.findByPublishedTrue()).thenReturn(List.of(a, b, c));
            stubAttempts(a, 1, 0);
            stubAttempts(b, 100, 50);
            stubAttempts(c, 50, 20);

            final List<LevelSummaryDto> result = service.getPublishedLevels(
                PublishedLevelSortBy.POPULARITY,
                DateRangePreset.AllTimeDateRangePreset.ALL_TIME);

            assertEquals(List.of("high-pop", "mid-pop", "low-pop"),
                result.stream().map(LevelSummaryDto::title).toList());
        }

        /// ALL_TIME period should populate popularity from the total play count.
        @Test
        @DisplayName("uses total play count as popularity when period is ALL_TIME")
        void allTimeUsesPlayCount() {
            final Level a = publishedLevel("a");
            when(levelRepository.findByPublishedTrue()).thenReturn(List.of(a));
            stubAttempts(a, 42, 10);

            final List<LevelSummaryDto> result = service.getPublishedLevels(
                PublishedLevelSortBy.POPULARITY,
                DateRangePreset.AllTimeDateRangePreset.ALL_TIME);

            assertEquals(42L, result.get(0).popularity());
        }

        /// The time-window query must not be issued when period is ALL_TIME.
        @Test
        @DisplayName("never queries the time-window repository when period is ALL_TIME")
        void allTimeSkipsTimeWindowQuery() {
            final Level a = publishedLevel("a");
            when(levelRepository.findByPublishedTrue()).thenReturn(List.of(a));
            stubAttempts(a, 42, 10);

            service.getPublishedLevels(
                PublishedLevelSortBy.POPULARITY,
                DateRangePreset.AllTimeDateRangePreset.ALL_TIME);

            verify(attemptRepository, never()).countByLevelAndTimestampAfter(any(), any());
        }

        /// A relative period should pull popularity from the time-window query.
        @Test
        @DisplayName("uses time-window play count as popularity when period is relative")
        void relativeUsesTimeWindowCount() {
            final Level a = publishedLevel("a");
            when(levelRepository.findByPublishedTrue()).thenReturn(List.of(a));
            stubAttempts(a, 100, 50);
            when(attemptRepository.countByLevelAndTimestampAfter(eq(a), any())).thenReturn(7L);

            final List<LevelSummaryDto> result = service.getPublishedLevels(
                PublishedLevelSortBy.POPULARITY,
                DateRangePreset.RelativeDateRangePreset.LAST_7_DAYS);

            assertEquals(7L, result.get(0).popularity());
        }

        /// Each level should trigger one time-window query when period is relative.
        @Test
        @DisplayName("queries the time-window repository once per level when period is relative")
        void queriesTimeWindowPerLevel() {
            final Level a = publishedLevel("a");
            final Level b = publishedLevel("b");
            when(levelRepository.findByPublishedTrue()).thenReturn(List.of(a, b));
            stubAttempts(a, 1, 0);
            stubAttempts(b, 1, 0);
            when(attemptRepository.countByLevelAndTimestampAfter(any(), any())).thenReturn(0L);

            service.getPublishedLevels(
                PublishedLevelSortBy.POPULARITY,
                DateRangePreset.RelativeDateRangePreset.LAST_30_DAYS);

            verify(attemptRepository).countByLevelAndTimestampAfter(eq(a), any());
            verify(attemptRepository).countByLevelAndTimestampAfter(eq(b), any());
        }
    }

    // --------------------------------------------------------------------
    // Summary content (independent of sort order)
    // --------------------------------------------------------------------

    /// Each summary should reflect the source level's title.
    @Test
    @DisplayName("populates the summary title from the level title")
    void summaryCarriesTitle() {
        final Level a = publishedLevel(SAMPLE_TITLE);
        when(levelRepository.findByPublishedTrue()).thenReturn(List.of(a));
        stubAttempts(a, 0, 0);

        final List<LevelSummaryDto> result = service.getPublishedLevels(
            PublishedLevelSortBy.CLEAR_RATE,
            DateRangePreset.AllTimeDateRangePreset.ALL_TIME);

        assertEquals(SAMPLE_TITLE, result.get(0).title());
    }

    /// Each summary should reflect the source level's description.
    @Test
    @DisplayName("populates the summary description from the level description")
    void summaryCarriesDescription() {
        final Level a = publishedLevel(SAMPLE_TITLE);
        when(levelRepository.findByPublishedTrue()).thenReturn(List.of(a));
        stubAttempts(a, 0, 0);

        final List<LevelSummaryDto> result = service.getPublishedLevels(
            PublishedLevelSortBy.CLEAR_RATE,
            DateRangePreset.AllTimeDateRangePreset.ALL_TIME);

        assertEquals(DESC, result.get(0).description());
    }

    /// Each summary should reflect the source level's creator name.
    @Test
    @DisplayName("populates the summary creator name from the level creator")
    void summaryCarriesCreatorName() {
        final Level a = publishedLevel(SAMPLE_TITLE);
        when(levelRepository.findByPublishedTrue()).thenReturn(List.of(a));
        stubAttempts(a, 0, 0);

        final List<LevelSummaryDto> result = service.getPublishedLevels(
            PublishedLevelSortBy.CLEAR_RATE,
            DateRangePreset.AllTimeDateRangePreset.ALL_TIME);

        assertEquals(CREATOR_NAME, result.get(0).creatorName());
    }

    /// One summary should be produced per published level.
    @Test
    @DisplayName("returns one summary per published level")
    void oneSummaryPerLevel() {
        final Level a = publishedLevel("a");
        final Level b = publishedLevel("b");
        final Level c = publishedLevel("c");
        when(levelRepository.findByPublishedTrue()).thenReturn(List.of(a, b, c));
        stubAttempts(a, 0, 0);
        stubAttempts(b, 0, 0);
        stubAttempts(c, 0, 0);

        final List<LevelSummaryDto> result = service.getPublishedLevels(
            PublishedLevelSortBy.CLEAR_RATE,
            DateRangePreset.AllTimeDateRangePreset.ALL_TIME);

        assertEquals(3, result.size());
    }

    /// The published-level repository should be hit exactly once per call.
    @Test
    @DisplayName("does not query published levels more than once per call")
    void publishedLevelsQueriedOnce() {
        when(levelRepository.findByPublishedTrue()).thenReturn(List.of());

        service.getPublishedLevels(
            PublishedLevelSortBy.POPULARITY,
            DateRangePreset.AllTimeDateRangePreset.ALL_TIME);

        verify(levelRepository).findByPublishedTrue();
    }

    // --------------------------------------------------------------------
    // Helper
    // --------------------------------------------------------------------

    /// Stubs the attempt repository to return the given play and clear counts.
    private void stubAttempts(final Level level, final long plays, final long clears) {
        lenient().when(attemptRepository.countByLevel(level)).thenReturn(plays);
        lenient().when(attemptRepository.countByLevelAndCompletedTrue(level)).thenReturn(clears);
    }
}
