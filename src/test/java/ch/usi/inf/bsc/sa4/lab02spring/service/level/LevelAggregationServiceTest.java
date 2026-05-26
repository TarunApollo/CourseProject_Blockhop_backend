package ch.usi.inf.bsc.sa4.lab02spring.service.level;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.LevelSummaryDto;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.PublishedLevelSearchCriteria;
import ch.usi.inf.bsc.sa4.lab02spring.model.AttemptVerificationStatus;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.repository.AttemptRepository;
import ch.usi.inf.bsc.sa4.lab02spring.repository.AttitudeRepository;
import ch.usi.inf.bsc.sa4.lab02spring.repository.LevelRepository;
import ch.usi.inf.bsc.sa4.lab02spring.service.UserService;
import ch.usi.inf.bsc.sa4.lab02spring.utils.DateRangePreset;
import ch.usi.inf.bsc.sa4.lab02spring.utils.PublishedLevelSortBy;

/// Unit tests for [LevelAggregationService].
/// Verifies sort strategies, popularity windows, and clear-rate computation.
@SpringBootTest
@DisplayName("The Level Aggregation Service")
class LevelAggregationServiceTest {

    /// Default level description used in fixtures.
    private static final String DESC = "desc";
    /// Default creator display name used in fixtures.
    private static final String CREATOR_NAME = "Mario";
    /// Default creator id used in fixtures.
    private static final String CREATOR_ID = "user-1";
    /// Sample level title used by metadata tests.
    private static final String SAMPLE_TITLE = "My Title";

    /// The service under test.
    @Autowired
    private LevelAggregationService service;

    /// Mocked level repository providing per-test fixtures.
    @MockitoBean
    private LevelRepository levelRepository;

    /// Mocked attempt repository providing aggregation counts.
    @MockitoBean
    private AttemptRepository attemptRepository;

    /// Mocked attitude repository providing likes/dislikes counts.
    @MockitoBean
    private AttitudeRepository attitudeRepository;

    /// Mocked user service used for resolving optional current-user attitudes.
    @MockitoBean
    private UserService userService;

    /// Shared creator used as the owner of fixture levels.
    private User creator;

    /// Initializes the level creator used by all tests.
    @BeforeEach
    void setUp() {
        service = new LevelAggregationService(levelRepository, attemptRepository,
                attitudeRepository, userService);
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
        Mockito.when(levelRepository.findByPublishedTrue()).thenReturn(List.of());

        final List<LevelSummaryDto> result = service.getPublishedLevels(PublishedLevelSortBy.CLEAR_RATE,
                DateRangePreset.AllTimeDateRangePreset.ALL_TIME);

        Assertions.assertEquals(List.of(), result);
    }

    /// Tests for clear-rate sort behavior.
    @Nested
    @DisplayName("when sorting by clear rate")
    class ClearRateSorting {

        /// Three levels with different clear ratios should be ordered descending.
        @Test
        @DisplayName("sorts levels by clear rate in descending order")
        void sortsDescending() {
            final Level a = publishedLevel("low-rate");
            final Level b = publishedLevel("high-rate");
            final Level c = publishedLevel("mid-rate");
            Mockito.when(levelRepository.findByPublishedTrue()).thenReturn(List.of(a, b, c));
            stubAttempts(a, 4, 1);
            stubAttempts(b, 10, 9);
            stubAttempts(c, 10, 5);

            final List<LevelSummaryDto> result = service.getPublishedLevels(
                    PublishedLevelSortBy.CLEAR_RATE,
                    DateRangePreset.AllTimeDateRangePreset.ALL_TIME);

            Assertions.assertEquals(List.of("high-rate", "mid-rate", "low-rate"),
                result.stream().map(LevelSummaryDto::title).toList());
            }

        /// playCount=0 should yield clearRate=0 to avoid a division by zero.
        @Test
        @DisplayName("computes clearRate as 0 when playCount is 0 to avoid division by zero")
        void clearRateZeroWhenNoPlays() {
            final Level a = publishedLevel("never-played");
            Mockito.when(levelRepository.findByPublishedTrue()).thenReturn(List.of(a));
            stubAttempts(a, 0, 0);

            final List<LevelSummaryDto> result = service.getPublishedLevels(
                    PublishedLevelSortBy.CLEAR_RATE,
                    DateRangePreset.AllTimeDateRangePreset.ALL_TIME);

            Assertions.assertEquals(0.0, result.get(0).clearRate());
        }

        /// 2 of 4 attempts completed should yield clearRate=0.5.
        @Test
        @DisplayName("computes clearRate as the ratio of completed attempts to total attempts")
        void clearRateIsCorrectRatio() {
            final Level a = publishedLevel("half-cleared");
            Mockito.when(levelRepository.findByPublishedTrue()).thenReturn(List.of(a));
            stubAttempts(a, 4, 2);

            final List<LevelSummaryDto> result = service.getPublishedLevels(
                    PublishedLevelSortBy.CLEAR_RATE,
                    DateRangePreset.AllTimeDateRangePreset.ALL_TIME);

            Assertions.assertEquals(0.5, result.get(0).clearRate());
        }

        /// Even with a relative window, the time-window query must not be used.
        @Test
        @DisplayName("never queries the time-window repository when sorting by clear rate")
        void doesNotQueryTimeWindow() {
            final Level a = publishedLevel("a");
            Mockito.when(levelRepository.findByPublishedTrue()).thenReturn(List.of(a));
            stubAttempts(a, 5, 3);

            service.getPublishedLevels(
                    PublishedLevelSortBy.CLEAR_RATE,
                    DateRangePreset.RelativeDateRangePreset.LAST_7_DAYS);

            Mockito.verify(attemptRepository, Mockito.never())
                    .countByLevelAndUserNotAndTimestampAfter(
                            ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
        }

        /// When sorting by clear rate, popularity should fall back to total play count.
        @Test
        @DisplayName("populates popularity with the total play count")
        void popularityFallsBackToPlayCount() {
            final Level a = publishedLevel("a");
            Mockito.when(levelRepository.findByPublishedTrue()).thenReturn(List.of(a));
            stubAttempts(a, 7, 3);

            final List<LevelSummaryDto> result = service.getPublishedLevels(
                    PublishedLevelSortBy.CLEAR_RATE,
                    DateRangePreset.AllTimeDateRangePreset.ALL_TIME);

            Assertions.assertEquals(7L, result.get(0).popularity());
        }
    }

    /// Tests for popularity sort behavior, including the time-window branch.
    @Nested
    @DisplayName("when sorting by popularity")
    class PopularitySorting {

        /// Three levels with different popularity values should be ordered descending.
        @Test
        @DisplayName("sorts levels by popularity in descending order")
        void sortsDescending() {
            final Level a = publishedLevel("low-pop");
            final Level b = publishedLevel("high-pop");
            final Level c = publishedLevel("mid-pop");
            Mockito.when(levelRepository.findByPublishedTrue()).thenReturn(List.of(a, b, c));
            stubAttempts(a, 1, 0);
            stubAttempts(b, 100, 50);
            stubAttempts(c, 50, 20);

            final List<LevelSummaryDto> result = service.getPublishedLevels(
                    PublishedLevelSortBy.POPULARITY,
                    DateRangePreset.AllTimeDateRangePreset.ALL_TIME);

            Assertions.assertEquals(List.of("high-pop", "mid-pop", "low-pop"),
                result.stream().map(LevelSummaryDto::title).toList());
        }

        /// ALL_TIME period should populate popularity from the total play count.
        @Test
        @DisplayName("uses total play count as popularity when period is ALL_TIME")
        void allTimeUsesPlayCount() {
            final Level a = publishedLevel("a");
            Mockito.when(levelRepository.findByPublishedTrue()).thenReturn(List.of(a));
            stubAttempts(a, 42, 10);

            final List<LevelSummaryDto> result = service.getPublishedLevels(
                    PublishedLevelSortBy.POPULARITY,
                    DateRangePreset.AllTimeDateRangePreset.ALL_TIME);

            Assertions.assertEquals(42L, result.get(0).popularity());
        }

        /// The time-window query must not be issued when period is ALL_TIME.
        @Test
        @DisplayName("never queries the time-window repository when period is ALL_TIME")
        void allTimeSkipsTimeWindowQuery() {
            final Level a = publishedLevel("a");
            Mockito.when(levelRepository.findByPublishedTrue()).thenReturn(List.of(a));
            stubAttempts(a, 42, 10);

            service.getPublishedLevels(
                    PublishedLevelSortBy.POPULARITY,
                    DateRangePreset.AllTimeDateRangePreset.ALL_TIME);

            Mockito.verify(attemptRepository, Mockito.never())
                    .countByLevelAndUserNotAndTimestampAfter(
                            ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
        }

        /// A relative period should pull popularity from the time-window query.
        @Test
        @DisplayName("uses time-window play count as popularity when period is relative")
        void relativeUsesTimeWindowCount() {
            final Level a = publishedLevel("a");
            Mockito.when(levelRepository.findByPublishedTrue()).thenReturn(List.of(a));
            stubAttempts(a, 100, 50);
            Mockito.when(attemptRepository.countByLevelAndUserNotAndTimestampAfter(
                    ArgumentMatchers.eq(a), ArgumentMatchers.eq(creator), ArgumentMatchers.any())).thenReturn(7L);

            final List<LevelSummaryDto> result = service.getPublishedLevels(
                    PublishedLevelSortBy.POPULARITY,
                    DateRangePreset.RelativeDateRangePreset.LAST_7_DAYS);

            Assertions.assertEquals(7L, result.get(0).popularity());
        }

        /// Each level should trigger one time-window query when period is relative.
        @Test
        @DisplayName("queries the time-window repository once per level when period is relative")
        void queriesTimeWindowPerLevel() {
            final Level a = publishedLevel("a");
            final Level b = publishedLevel("b");
            Mockito.when(levelRepository.findByPublishedTrue()).thenReturn(List.of(a, b));
            stubAttempts(a, 1, 0);
            stubAttempts(b, 1, 0);
            Mockito.when(attemptRepository.countByLevelAndUserNotAndTimestampAfter(
                    ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(0L);

            service.getPublishedLevels(
                    PublishedLevelSortBy.POPULARITY,
                    DateRangePreset.RelativeDateRangePreset.LAST_30_DAYS);

            Mockito.verify(attemptRepository).countByLevelAndUserNotAndTimestampAfter(
                    ArgumentMatchers.eq(a), ArgumentMatchers.eq(creator), ArgumentMatchers.any());
            Mockito.verify(attemptRepository).countByLevelAndUserNotAndTimestampAfter(
                    ArgumentMatchers.eq(b), ArgumentMatchers.eq(creator), ArgumentMatchers.any());
        }
    }

    /// Tests verifying summary content independent of sort order.
    @Nested
    @DisplayName("when building summary content")
    class SummaryContent {

        /// Each summary should reflect the source level's title.
        @Test
        @DisplayName("populates the summary title from the level title")
        void summaryCarriesTitle() {
            final Level a = publishedLevel(SAMPLE_TITLE);
            Mockito.when(levelRepository.findByPublishedTrue()).thenReturn(List.of(a));
            stubAttempts(a, 0, 0);

            final List<LevelSummaryDto> result = service.getPublishedLevels(
                PublishedLevelSortBy.CLEAR_RATE,
                DateRangePreset.AllTimeDateRangePreset.ALL_TIME);

            Assertions.assertEquals(SAMPLE_TITLE, result.get(0).title());
        }

        /// Each summary should reflect the source level's description.
        @Test
        @DisplayName("populates the summary description from the level description")
        void summaryCarriesDescription() {
            final Level a = publishedLevel(SAMPLE_TITLE);
            Mockito.when(levelRepository.findByPublishedTrue()).thenReturn(List.of(a));
            stubAttempts(a, 0, 0);

            final List<LevelSummaryDto> result = service.getPublishedLevels(
                PublishedLevelSortBy.CLEAR_RATE,
                DateRangePreset.AllTimeDateRangePreset.ALL_TIME);

            Assertions.assertEquals(DESC, result.get(0).description());
        }

        /// Each summary should reflect the source level's creator name.
        @Test
        @DisplayName("populates the summary creator name from the level creator")
        void summaryCarriesCreatorName() {
            final Level a = publishedLevel(SAMPLE_TITLE);
            Mockito.when(levelRepository.findByPublishedTrue()).thenReturn(List.of(a));
            stubAttempts(a, 0, 0);

            final List<LevelSummaryDto> result = service.getPublishedLevels(
                PublishedLevelSortBy.CLEAR_RATE,
                DateRangePreset.AllTimeDateRangePreset.ALL_TIME);

            Assertions.assertEquals(CREATOR_NAME, result.get(0).creatorName());
        }

        /// One summary should be produced per published level.
        @Test
        @DisplayName("returns one summary per published level")
        void oneSummaryPerLevel() {
            final Level a = publishedLevel("a");
            final Level b = publishedLevel("b");
            final Level c = publishedLevel("c");
            Mockito.when(levelRepository.findByPublishedTrue()).thenReturn(List.of(a, b, c));
            stubAttempts(a, 0, 0);
            stubAttempts(b, 0, 0);
            stubAttempts(c, 0, 0);

            final List<LevelSummaryDto> result = service.getPublishedLevels(
                PublishedLevelSortBy.CLEAR_RATE,
                DateRangePreset.AllTimeDateRangePreset.ALL_TIME);

            Assertions.assertEquals(3, result.size());
        }

        /// When a current user is present, the summary should expose whether they
        /// have completed the level at least once.
        @Test
        @DisplayName("populates whether the current user has completed the level")
        void summaryCarriesCurrentUserCompletionFlag() {
            final Level a = publishedLevel("a");
            final User currentUser = new User("player-2", "Luigi");
            Mockito.when(levelRepository.findByPublishedTrue()).thenReturn(List.of(a));
            Mockito.when(userService.getById(currentUser.getId())).thenReturn(Optional.of(currentUser));
            stubAttempts(a, 0, 0);
            Mockito.when(attemptRepository.existsByUserAndLevelAndCompletedTrueAndAntiCheatStatus(
                    currentUser,
                    a,
                    AttemptVerificationStatus.LEGIT)).thenReturn(true);

            final List<LevelSummaryDto> result = service.getPublishedLevels(
                    PublishedLevelSortBy.CLEAR_RATE,
                    DateRangePreset.AllTimeDateRangePreset.ALL_TIME,
                    null,
                    currentUser.getId());

            Assertions.assertTrue(result.get(0).completedByCurrentUser());
        }

        /// Non-LEGIT completions must not unlock the ghost-related summary flag.
        @Test
        @DisplayName("does not mark the level as completed for ghost access when no LEGIT completion exists")
        void summaryRequiresLegitCompletionFlag() {
            final Level a = publishedLevel("a");
            final User currentUser = new User("player-2", "Luigi");
            Mockito.when(levelRepository.findByPublishedTrue()).thenReturn(List.of(a));
            Mockito.when(userService.getById(currentUser.getId())).thenReturn(Optional.of(currentUser));
            stubAttempts(a, 0, 0);
            Mockito.when(attemptRepository.existsByUserAndLevelAndCompletedTrueAndAntiCheatStatus(
                    currentUser,
                    a,
                    AttemptVerificationStatus.LEGIT)).thenReturn(false);

            final List<LevelSummaryDto> result = service.getPublishedLevels(
                    PublishedLevelSortBy.CLEAR_RATE,
                    DateRangePreset.AllTimeDateRangePreset.ALL_TIME,
                    null,
                    currentUser.getId());

            Assertions.assertFalse(result.get(0).completedByCurrentUser());
        }

        /// The published-level repository should be hit exactly once per call.
        @Test
        @DisplayName("does not query published levels more than once per call")
        void publishedLevelsQueriedOnce() {
            Mockito.when(levelRepository.findByPublishedTrue()).thenReturn(List.of());

            service.getPublishedLevels(
                PublishedLevelSortBy.POPULARITY,
                DateRangePreset.AllTimeDateRangePreset.ALL_TIME);

            Mockito.verify(levelRepository).findByPublishedTrue();
        }
    }

    /// Tests the service wiring around published-level search criteria filtering.
    @Nested
    @DisplayName("when filtering published levels")
    class SearchCriteriaFiltering {
        /// Filtering should happen before sorting.
        @Test
        @DisplayName("sorts the filtered result set")
        void sortsAfterFiltering() {
            final Level low = publishedLevel("low");
            final Level mid = publishedLevel("mid");
            final Level high = publishedLevel("high");
            Mockito.when(levelRepository.findByPublishedTrue()).thenReturn(List.of(low, mid, high));
            stubAttempts(low, 10, 2);
            stubAttempts(mid, 10, 5);
            stubAttempts(high, 10, 9);

            final PublishedLevelSearchCriteria criteria = new PublishedLevelSearchCriteria(
                    0.3, null, null, null, null, null, null, null, null);

            final List<LevelSummaryDto> result = service.getPublishedLevels(
                    PublishedLevelSortBy.CLEAR_RATE,
                    DateRangePreset.AllTimeDateRangePreset.ALL_TIME,
                    criteria,
                    null);

            Assertions.assertEquals(List.of("high", "mid"), result.stream().map(LevelSummaryDto::title).toList());
        }

        /// Only levels whose description contains the query should remain.
        @Test
        @DisplayName("keeps only levels whose description contains the query")
        void filtersByDescription() {
            final Level castle = new Level("castle-level", "A spooky castle", creator);
            final Level forest = new Level("forest-level", "A green forest", creator);
            Mockito.when(levelRepository.findByPublishedTrue()).thenReturn(List.of(castle, forest));
            stubAttempts(castle, 10, 5);
            stubAttempts(forest, 10, 5);

            final PublishedLevelSearchCriteria criteria = new PublishedLevelSearchCriteria(
                    null, null, null, null, null, null, null, null, "castle");

            final List<LevelSummaryDto> result = service.getPublishedLevels(
                    PublishedLevelSortBy.CLEAR_RATE,
                    DateRangePreset.AllTimeDateRangePreset.ALL_TIME,
                    criteria,
                    null);

            Assertions.assertEquals(
                    List.of("castle-level"),
                    result.stream().map(LevelSummaryDto::title).toList());
        }

    }

    /// Tests for selecting a random published level.
    @Nested
    @DisplayName("when selecting a random published level")
    class RandomPublishedLevel {

        /// No matching levels should produce an empty Optional.
        @Test
        @DisplayName("returns empty when no levels match")
        void returnsEmptyWhenNoLevelsMatch() {
            Mockito.when(levelRepository.findByPublishedTrue()).thenReturn(List.of());

            final Optional<LevelSummaryDto> result = service.getRandomPublishedLevel(
                    PublishedLevelSortBy.POPULARITY,
                    DateRangePreset.AllTimeDateRangePreset.ALL_TIME,
                    new PublishedLevelSearchCriteria(null, null, null, null, null, null, null, null, null),
                    null);

            Assertions.assertTrue(result.isEmpty());
        }

        /// With one matching level, random selection should return that level.
        @Test
        @DisplayName("returns the only matching level")
        void returnsOnlyMatchingLevel() {
            final Level excluded = publishedLevel("excluded");
            final Level included = publishedLevel("included");
            Mockito.when(levelRepository.findByPublishedTrue()).thenReturn(List.of(excluded, included));
            stubAttempts(excluded, 1, 0);
            stubAttempts(included, 10, 5);

            final Optional<LevelSummaryDto> result = service.getRandomPublishedLevel(
                    PublishedLevelSortBy.POPULARITY,
                    DateRangePreset.AllTimeDateRangePreset.ALL_TIME,
                    new PublishedLevelSearchCriteria(null, null, 5L, null, null, null, null, null, null),
                    null);

            Assertions.assertTrue(result.isPresent());
            Assertions.assertEquals("included", result.orElseThrow().title());
        }
    }

    /// Stubs the attempt repository to return the given play and clear counts.
    private void stubAttempts(final Level level, final long plays, final long clears) {
        Mockito.when(attemptRepository.countByLevelAndUserNot(level, level.getCreator())).thenReturn(plays);
        Mockito.when(attemptRepository.countByLevelAndUserNotAndCompletedTrue(level, level.getCreator()))
                .thenReturn(clears);
    }

}
