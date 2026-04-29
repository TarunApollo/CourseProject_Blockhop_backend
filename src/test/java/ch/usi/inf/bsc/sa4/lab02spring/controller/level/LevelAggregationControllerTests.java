package ch.usi.inf.bsc.sa4.lab02spring.controller.level;

import ch.usi.inf.bsc.sa4.lab02spring.configuration.ControllerSecurityTestConfig;
import ch.usi.inf.bsc.sa4.lab02spring.configuration.ControllerSecurityTestSupport;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.LevelSummaryDto;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.service.level.LevelAggregationService;
import ch.usi.inf.bsc.sa4.lab02spring.utils.DateRangePreset;
import ch.usi.inf.bsc.sa4.lab02spring.utils.PublishedLevelSortBy;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.List;

/// Black-box tests for [LevelAggregationController] endpoints.
/// Verifies the retrieval and sorting of published level summaries.
@WebMvcTest(controllers = LevelAggregationController.class)
@AutoConfigureRestTestClient
@Import(ControllerSecurityTestConfig.class)
@DisplayName("The Level Aggregation Controller")
class LevelAggregationControllerTests {

    /// The fake authenticated user ID used across tests.
    private static final String USER_ID = "userid1";

    /// The fake authenticated user's display name.
    private static final String USER_NAME = "Test User";

    /// Mocked service for level aggregation.
    @MockitoBean
    private LevelAggregationService levelAggregationService;

    /// Mocked decoder used by the resource-server security filter.
    @MockitoBean
    private JwtDecoder jwtDecoder;

    /// Client used to perform REST calls.
    @Autowired
    private RestTestClient restTestClient;

    /// Shared test level instance.
    private static Level testLevel;

    /// Shared test summary instance.
    private static LevelSummaryDto testSummary;

    /// Initializes static test data.
    @BeforeAll
    static void setupData() {
        final User creator = new User("c1", "Creator");
        testLevel = new Level("Title", "Desc", creator);
        testSummary = new LevelSummaryDto(testLevel, 10, 0.5, 5);
    }

    /// Configures the mocked JWT decoder before each test.
    @BeforeEach
    void setupJwt() {
        ControllerSecurityTestSupport.mockJwtDecoder(this.jwtDecoder, USER_ID, USER_NAME);
    }

    /// Tests for GET /levels/published.
    @Nested
    @DisplayName("GET /levels/published")
    class GetPublishedLevels {

        /// Verifies that the endpoint returns a list of level summaries with 200 OK.
        @Test
        @DisplayName("should return 200 OK and list of summaries")
        void returnsSummaries() {
            final List<LevelSummaryDto> expected = List.of(testSummary);
            Mockito.when(levelAggregationService.getPublishedLevels(
                    ArgumentMatchers.eq(PublishedLevelSortBy.POPULARITY),
                    ArgumentMatchers.eq(DateRangePreset.AllTimeDateRangePreset.ALL_TIME)))
                    .thenReturn(expected);

            ControllerSecurityTestSupport.withAuthAndCsrf(restTestClient.get().uri("/levels/published?sortBy=POPULARITY"))
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(new ParameterizedTypeReference<List<LevelSummaryDto>>() {})
                    .isEqualTo(expected);
        }

        /// Verifies that the endpoint returns an empty list when no levels are published.
        @Test
        @DisplayName("should return 200 OK and empty list when no levels found")
        void returnsEmptyList() {
            Mockito.when(levelAggregationService.getPublishedLevels(
                    ArgumentMatchers.eq(PublishedLevelSortBy.CLEAR_RATE),
                    ArgumentMatchers.any()))
                    .thenReturn(List.of());

            ControllerSecurityTestSupport.withAuthAndCsrf(restTestClient.get().uri("/levels/published?sortBy=CLEAR_RATE"))
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(new ParameterizedTypeReference<List<LevelSummaryDto>>() {})
                    .isEqualTo(List.of());
        }

        /// Verifies that specifying a period works as expected.
        @Test
        @DisplayName("should return 200 OK when period is specified")
        void returnsSummariesWithPeriod() {
            final List<LevelSummaryDto> expected = List.of(testSummary);
            Mockito.when(levelAggregationService.getPublishedLevels(
                    ArgumentMatchers.eq(PublishedLevelSortBy.POPULARITY),
                    ArgumentMatchers.eq(DateRangePreset.RelativeDateRangePreset.LAST_7_DAYS)))
                    .thenReturn(expected);

            ControllerSecurityTestSupport.withAuthAndCsrf(restTestClient.get().uri("/levels/published?sortBy=POPULARITY&period=LAST_7_DAYS"))
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(new ParameterizedTypeReference<List<LevelSummaryDto>>() {})
                    .isEqualTo(expected);
        }
    }
}
