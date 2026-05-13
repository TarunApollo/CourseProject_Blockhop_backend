package ch.usi.inf.bsc.sa4.lab02spring.controller.level;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.LevelSummaryDto;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.service.level.LevelAggregationService;
import ch.usi.inf.bsc.sa4.lab02spring.utils.DateRangePreset;
import ch.usi.inf.bsc.sa4.lab02spring.utils.PublishedLevelSortBy;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientAutoConfiguration;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.List;

/// Black-box tests for LevelAggregationController. Verifies the contract for
/// fetching sorted summaries of published levels.
///
@WebMvcTest(controllers = LevelAggregationController.class, excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                OAuth2ClientAutoConfiguration.class,
                OAuth2ResourceServerAutoConfiguration.class
})
@AutoConfigureRestTestClient
@DisplayName("Level Aggregation Controller Logic Tests")
class LevelAggregationControllerTests {

        /// The mocked aggregation service.
        @MockitoBean
        private LevelAggregationService levelAggregationService;

        /// The RestTestClient for performing requests.
        @Autowired
        private RestTestClient restTestClient;

        /// Shared test level instance.
        private static Level testLevel;

        /// Initializes static test data.
        @BeforeAll
        static void setupData() {
                final User testUser = new User("userid1", "Test User");
                testLevel = new Level("Test Level", "A description", testUser);
        }

        /// Verifies that getting published levels returns a list sorted by CLEAR_RATE.
        @Test
        @DisplayName("should return list of published levels sorted by CLEAR_RATE")
        void testGetPublishedLevels() {
                final List<LevelSummaryDto> summaries = List.of(
                                new LevelSummaryDto(testLevel, 10, 0.5, 5, 3, 2));
                Mockito.when(levelAggregationService.getPublishedLevels(
                                Mockito.eq(PublishedLevelSortBy.CLEAR_RATE),
                                Mockito.any(DateRangePreset.class)))
                                .thenReturn(summaries);

                Assertions.assertDoesNotThrow(() -> restTestClient.get()
                                .uri("/levels/published?sortBy=CLEAR_RATE&period=ALL_TIME")
                                .exchange()
                                .expectStatus().isOk()
                                .expectBody(new ParameterizedTypeReference<List<LevelSummaryDto>>() {
                                })
                                .isEqualTo(summaries));
        }
}
