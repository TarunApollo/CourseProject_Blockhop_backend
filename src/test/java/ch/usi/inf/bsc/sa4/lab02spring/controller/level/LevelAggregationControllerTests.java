package ch.usi.inf.bsc.sa4.lab02spring.controller.level;

import ch.usi.inf.bsc.sa4.lab02spring.configuration.ControllerSecurityTestConfig;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.LevelSummaryDto;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.service.level.LevelAggregationService;
import ch.usi.inf.bsc.sa4.lab02spring.utils.DateRangePreset;
import ch.usi.inf.bsc.sa4.lab02spring.utils.PublishedLevelSortBy;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.List;

/// Black-box tests for [LevelAggregationController] endpoints. Verifies the
/// retrieval and sorting of published level summaries.
@SpringBootTest
@AutoConfigureMockMvc
@Import(ControllerSecurityTestConfig.class)
@SuppressWarnings("PMD.UnitTestShouldIncludeAssert")
@DisplayName("The Level Aggregation Controller")
class LevelAggregationControllerTests {

    /// Mocked service for level aggregation.
    @MockitoBean
    private LevelAggregationService levelAggregationService;

    /// Client used to perform REST calls.
    @Autowired
    private RestTestClient restTestClient;

    /// Shared test summary instance.
    private static LevelSummaryDto testSummary;

    /// Initializes static test data.
    @BeforeAll
    static void setupData() {
        final User creator = new User("c1", "Creator");
        final Level testLevel = new Level("Title", "Desc", creator);
        testSummary = new LevelSummaryDto(testLevel, 10, 0.5, 5);
    }

    /// Tests for GET /levels/published.
    @Nested
    @DisplayName("GET /levels/published")
    class GetPublishedLevels {

        /// Verifies that the endpoint returns a list of level summaries with 200 OK.
        @Test
        @DisplayName("should return 200 OK and list of summaries sorted by popularity")
        void returnsSummariesSortedByPopularity() {
            final List<LevelSummaryDto> expected = List.of(testSummary);
            Mockito.when(levelAggregationService.getPublishedLevels(
                    ArgumentMatchers.eq(PublishedLevelSortBy.POPULARITY),
                    ArgumentMatchers.eq(DateRangePreset.AllTimeDateRangePreset.ALL_TIME),
                    ArgumentMatchers.eq(ControllerSecurityTestConfig.DEFAULT_USER_ID)))
                    .thenReturn(expected);

            restTestClient.get().uri(
                    "/levels/published?sortBy=POPULARITY")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(new ParameterizedTypeReference<List<LevelSummaryDto>>() {
                    })
                    .isEqualTo(expected);
        }

        /// Verifies that sorting by clear rate works correctly.
        @Test
        @DisplayName("should return 200 OK and list of summaries sorted by clear rate")
        void returnsSummariesSortedByClearRate() {
            final List<LevelSummaryDto> expected = List.of(testSummary);
            Mockito.when(levelAggregationService.getPublishedLevels(
                    ArgumentMatchers.eq(PublishedLevelSortBy.CLEAR_RATE),
                    ArgumentMatchers.eq(DateRangePreset.AllTimeDateRangePreset.ALL_TIME),
                    ArgumentMatchers.eq(ControllerSecurityTestConfig.DEFAULT_USER_ID)))
                    .thenReturn(expected);

            restTestClient.get().uri(
                    "/levels/published?sortBy=CLEAR_RATE")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(new ParameterizedTypeReference<List<LevelSummaryDto>>() {
                    })
                    .isEqualTo(expected);
        }

        /// Verifies that the endpoint returns an empty list when no levels
        /// are published.
        @Test
        @DisplayName("should return 200 OK and empty list when no levels found")
        void returnsEmptyList() {
            Mockito.when(levelAggregationService.getPublishedLevels(
                    ArgumentMatchers.eq(PublishedLevelSortBy.CLEAR_RATE),
                    ArgumentMatchers.any(),
                    ArgumentMatchers.eq(ControllerSecurityTestConfig.DEFAULT_USER_ID)))
                    .thenReturn(List.of());

            restTestClient.get().uri(
                    "/levels/published?sortBy=CLEAR_RATE")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(new ParameterizedTypeReference<List<LevelSummaryDto>>() {
                    })
                    .isEqualTo(List.of());
        }

        /// Verifies that specifying a relative period works as expected.
        @Test
        @DisplayName("should return 200 OK with summaries when a relative period is specified")
        void returnsSummariesWithRelativePeriod() {
            final List<LevelSummaryDto> expected = List.of(testSummary);
            Mockito.when(levelAggregationService.getPublishedLevels(
                    ArgumentMatchers.eq(PublishedLevelSortBy.POPULARITY),
                    ArgumentMatchers.eq(
                            DateRangePreset.RelativeDateRangePreset.LAST_7_DAYS),
                    ArgumentMatchers.eq(ControllerSecurityTestConfig.DEFAULT_USER_ID)))
                    .thenReturn(expected);

            restTestClient.get().uri(
                    "/levels/published?sortBy=POPULARITY&period=LAST_7_DAYS")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(new ParameterizedTypeReference<List<LevelSummaryDto>>() {
                    })
                    .isEqualTo(expected);
        }

        /// Verifies that a missing required sortBy parameter returns 400 Bad Request.
        @Test
        @DisplayName("should return 400 Bad Request when sortBy parameter is missing")
        void missingSortByReturnsBadRequest() {
            restTestClient.get().uri("/levels/published")
                    .exchange()
                    .expectStatus().isBadRequest();
        }

        /// Verifies that an invalid sortBy value returns 400 Bad Request because the
        /// service throws IllegalStateException.
        @Test
        @DisplayName("should return 400 Bad Request when sortBy has an invalid value")
        void invalidSortByReturnsBadRequest() {
            Mockito.when(levelAggregationService.getPublishedLevels(
                    ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
                    .thenThrow(new IllegalStateException(
                            "Unsupported published level sort: INVALID"));

            restTestClient.get().uri(
                    "/levels/published?sortBy=INVALID")
                    .exchange()
                    .expectStatus().isBadRequest();
        }
    }
}
