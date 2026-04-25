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

@DisplayName("LevelAggregationService.getPublishedLevels")
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NullAway")
class LevelAggregationServiceTest {

    @Mock private LevelRepository levelRepository;
    @Mock private AttemptRepository attemptRepository;

    @InjectMocks private LevelAggregationService service;

    private User creator;

    @BeforeEach
    void setUp() {
        creator = new User("user-1", "Mario");
    }

    private Level publishedLevel(final String title) {
        final Level level = new Level(title, "desc", creator);
        return level;
    }

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

    @Nested
    @DisplayName("when sortBy is CLEAR_RATE")
    class ClearRateSorting {

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

    @Nested
    @DisplayName("when sortBy is POPULARITY")
    class PopularitySorting {

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
            verify(attemptRepository, never()).countByLevelAndTimestampAfter(any(), any());
        }

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

    @Test
    @DisplayName("populates each summary with the level title, description, and creator name")
    void summaryCarriesLevelMetadata() {
        final Level a = publishedLevel("My Title");
        when(levelRepository.findByPublishedTrue()).thenReturn(List.of(a));
        stubAttempts(a, 0, 0);

        final List<LevelSummaryDto> result = service.getPublishedLevels(
            PublishedLevelSortBy.CLEAR_RATE,
            DateRangePreset.AllTimeDateRangePreset.ALL_TIME);

        final LevelSummaryDto dto = result.get(0);
        assertEquals("My Title", dto.title());
        assertEquals("desc", dto.description());
        assertEquals("Mario", dto.creatorName());
    }

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

    private void stubAttempts(final Level level, final long plays, final long clears) {
        lenient().when(attemptRepository.countByLevel(level)).thenReturn(plays);
        lenient().when(attemptRepository.countByLevelAndCompletedTrue(level)).thenReturn(clears);
    }
}
