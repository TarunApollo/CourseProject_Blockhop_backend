package ch.usi.inf.bsc.sa4.lab02spring.controller;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.AttemptDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.CloneLevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.CreateLevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.LevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.LevelSummaryDto;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.UpdateLevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.converter.LayerToTiledMapConverter;
import ch.usi.inf.bsc.sa4.lab02spring.model.ClearCondition;
import ch.usi.inf.bsc.sa4.lab02spring.model.Condition;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.Position;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.service.AttemptService;
import ch.usi.inf.bsc.sa4.lab02spring.service.LevelService;
import ch.usi.inf.bsc.sa4.lab02spring.service.TileSetService;
import ch.usi.inf.bsc.sa4.lab02spring.service.UserService;
import ch.usi.inf.bsc.sa4.lab02spring.utils.AuthUtils;
import ch.usi.inf.bsc.sa4.lab02spring.utils.DateRangePreset;
import ch.usi.inf.bsc.sa4.lab02spring.utils.PublishedLevelSortBy;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
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
import java.util.Map;
import java.util.Optional;

// Black-box tests for LevelController.
// Tests HTTP contract: status codes, response bodies, and content types.
//
// NOTE: Error Prone crashes when MockedStatic and assertDoesNotThrow are
// combined with a RestTestClient fluent chain. Tests that mock AuthUtils use
// returnResult().getStatus() directly and assert on status via assertEquals.
//
// NOTE: PUT /{levelId}/thumbnail is skipped because it is @Deprecated and
// requires multipart upload (not supported by RestTestClient).
@SuppressWarnings("PMD.ExcessiveImports")
@WebMvcTest(controllers = LevelController.class, excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        OAuth2ClientAutoConfiguration.class,
        OAuth2ResourceServerAutoConfiguration.class
})
@AutoConfigureRestTestClient
@DisplayName("Level Controller Logic Tests")
/* package */ class LevelControllerLogicTests {

    /** The authenticated user ID used across tests. */
    private static final String USER_ID = "userid1";

    /** A fixed timestamp used for attempt tests. */
    private static final java.time.ZonedDateTime FIXED_TIMESTAMP =
            java.time.ZonedDateTime.of(2025, 1, 1, 0, 0, 0, 0, java.time.ZoneOffset.UTC);

    /** A fixed duration used for attempt tests. */
    private static final java.time.Duration FIXED_DURATION = java.time.Duration.ofSeconds(30);

    /** The authenticated user name used across tests. */
    private static final String USER_NAME = "Test User";

    /** A level ID used across tests. */
    private static final String LEVEL_ID = "level-1";

    /** The mocked level service. */
    @MockitoBean
    private LevelService levelService;

    /** The mocked user service. */
    @MockitoBean
    private UserService userService;

    /** The mocked tile set service. */
    @MockitoBean
    private TileSetService tileSetService;

    /** The mocked attempt service. */
    @MockitoBean
    private AttemptService attemptService;

    /** The mocked layer to tiled map converter. */
    @MockitoBean
    private LayerToTiledMapConverter layerToTiledMapConverter;

    /** The RestTestClient for performing requests. */
    @Autowired
    private RestTestClient restTestClient;

    /** The test user. */
    private static User testUser;

    /** A test level owned by the test user. */
    private static Level testLevel;

    // Initializes static test data.
    @BeforeAll
    static void setupData() {
        testUser = new User(USER_ID, USER_NAME);
        testLevel = new Level("Test Level", "A description", testUser);
    }

    /** Tests for POST /levels. */
    @Nested
    @DisplayName("POST /levels")
    /* default */ class CreateLevel {

        // Verifies that creating a level returns 200 OK.
        @Test
        @DisplayName("should return 200 OK with created level")
        void testCreateLevelReturnsOk() {
            try (MockedStatic<AuthUtils> mockedAuth = Mockito.mockStatic(AuthUtils.class)) {
                mockedAuth.when(() -> AuthUtils.getUserIdFromAuth(Mockito.any()))
                        .thenReturn(USER_ID);
                Mockito.when(levelService.createLevel(Mockito.any(CreateLevelDTO.class), Mockito.eq(USER_ID)))
                        .thenReturn(testLevel);

                final CreateLevelDTO dto = new CreateLevelDTO("Test Level", "A description");
                final var status = restTestClient.post().uri("/levels")
                        .body(dto)
                        .exchange()
                        .returnResult(LevelDTO.class)
                        .getStatus();
                Assertions.assertEquals(org.springframework.http.HttpStatus.OK, status);
            }
        }
    }

    /** Tests for GET /levels/published. */
    @Nested
    @DisplayName("GET /levels/published")
    /* default */ class GetPublishedLevels {

        // Verifies that getting published levels returns a list sorted by CLEAR_RATE.
        @Test
        @DisplayName("should return list of published levels sorted by CLEAR_RATE")
        void testGetPublishedLevelsByClearRate() {
            final List<LevelSummaryDto> summaries = List.of(
                    new LevelSummaryDto(testLevel, 10, 0.5, 5));
            Mockito.when(levelService.getPublishedLevels(
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

    /** Tests for GET /levels/{levelId}/thumbnail. */
    @Nested
    @DisplayName("GET /levels/{levelId}/thumbnail")
    /* default */ class GetThumbnail {

        // Verifies that getting a thumbnail returns 200 OK with image/png content.
        @Test
        @DisplayName("should return 200 OK with image/png content type")
        void testGetThumbnailReturnsImage() {
            final byte[] thumbnailBytes = new byte[]{(byte) 0x89, 'P', 'N', 'G'};
            Mockito.when(levelService.getThumbnailForLevel(LEVEL_ID))
                    .thenReturn(thumbnailBytes);

            Assertions.assertDoesNotThrow(() -> restTestClient.get()
                    .uri("/levels/{levelId}/thumbnail", LEVEL_ID)
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().contentType("image/png"));
        }
    }

    /** Tests for POST /levels/clone. */
    @Nested
    @DisplayName("POST /levels/clone")
    /* default */ class CloneLevel {

        // Verifies that cloning a level returns 200 OK.
        @Test
        @DisplayName("should return 200 OK with cloned level")
        void testCloneLevelReturnsOk() {
            try (MockedStatic<AuthUtils> mockedAuth = Mockito.mockStatic(AuthUtils.class)) {
                mockedAuth.when(() -> AuthUtils.getUserIdFromAuth(Mockito.any()))
                        .thenReturn(USER_ID);
                Mockito.when(userService.getById(USER_ID))
                        .thenReturn(Optional.of(testUser));
                Mockito.when(levelService.cloneLevel(Mockito.any(CloneLevelDTO.class), Mockito.eq(testUser)))
                        .thenReturn(Optional.of(testLevel));

                final CloneLevelDTO dto = new CloneLevelDTO(LEVEL_ID);
                final var status = restTestClient.post().uri("/levels/clone")
                        .body(dto)
                        .exchange()
                        .returnResult(LevelDTO.class)
                        .getStatus();
                Assertions.assertEquals(org.springframework.http.HttpStatus.OK, status);
            }
        }

        // Verifies that cloning a level not owned by the user returns 403 Forbidden.
        @Test
        @DisplayName("should return 403 Forbidden when clone is not allowed")
        void testCloneLevelReturnsForbidden() {
            try (MockedStatic<AuthUtils> mockedAuth = Mockito.mockStatic(AuthUtils.class)) {
                mockedAuth.when(() -> AuthUtils.getUserIdFromAuth(Mockito.any()))
                        .thenReturn(USER_ID);
                Mockito.when(userService.getById(USER_ID))
                        .thenReturn(Optional.of(testUser));
                Mockito.when(levelService.cloneLevel(Mockito.any(CloneLevelDTO.class), Mockito.eq(testUser)))
                        .thenReturn(Optional.empty());

                final CloneLevelDTO dto = new CloneLevelDTO(LEVEL_ID);
                final var status = restTestClient.post().uri("/levels/clone")
                        .body(dto)
                        .exchange()
                        .returnResult(LevelDTO.class)
                        .getStatus();
                Assertions.assertEquals(org.springframework.http.HttpStatus.FORBIDDEN, status);
            }
        }
    }

    /** Tests for PUT /levels/{levelId}/properties. */
    @Nested
    @DisplayName("PUT /levels/{levelId}/properties")
    /* default */ class UpdateLevelProperties {

        // Verifies that updating level properties returns 200 OK.
        @Test
        @DisplayName("should return 200 OK with updated level")
        void testUpdateLevelReturnsOk() {
            try (MockedStatic<AuthUtils> mockedAuth = Mockito.mockStatic(AuthUtils.class)) {
                mockedAuth.when(() -> AuthUtils.getUserIdFromAuth(Mockito.any()))
                        .thenReturn(USER_ID);
                Mockito.when(userService.getById(USER_ID))
                        .thenReturn(Optional.of(testUser));
                Mockito.when(levelService.updateLevelProperties(
                        Mockito.eq(testUser), Mockito.eq(LEVEL_ID), Mockito.any()))
                        .thenReturn(testLevel);

                final UpdateLevelDTO dto = new UpdateLevelDTO(
                        Optional.of("Updated"), Optional.empty(),
                        Optional.of(new ClearCondition(new Condition.NoClearCondition(), 0)));
                final var status = restTestClient.put()
                        .uri("/levels/{levelId}/properties", LEVEL_ID)
                        .body(dto)
                        .exchange()
                        .returnResult(LevelDTO.class)
                        .getStatus();
                Assertions.assertEquals(org.springframework.http.HttpStatus.OK, status);
            }
        }
    }

    /** Tests for DELETE /levels/{levelId}. */
    @Nested
    @DisplayName("DELETE /levels/{levelId}")
    /* default */ class DeleteLevel {

        // Verifies that deleting a level returns 204 No Content.
        @Test
        @DisplayName("should return 204 No Content on successful deletion")
        void testDeleteLevelReturnsNoContent() {
            try (MockedStatic<AuthUtils> mockedAuth = Mockito.mockStatic(AuthUtils.class)) {
                mockedAuth.when(() -> AuthUtils.getUserIdFromAuth(Mockito.any()))
                        .thenReturn(USER_ID);
                Mockito.doNothing().when(levelService)
                        .deleteLevel(Mockito.eq(USER_ID), Mockito.eq(LEVEL_ID));

                final var status = restTestClient.delete()
                        .uri("/levels/{levelId}", LEVEL_ID)
                        .exchange()
                        .returnResult(Void.class)
                        .getStatus();
                Assertions.assertEquals(org.springframework.http.HttpStatus.NO_CONTENT, status);
            }
        }
    }

    /** Tests for PUT /levels/{levelId}/unpublish. */
    @Nested
    @DisplayName("PUT /levels/{levelId}/unpublish")
    /* default */ class UnpublishLevel {

        // Verifies that unpublishing a level returns 200 OK.
        @Test
        @DisplayName("should return 200 OK with unpublished level")
        void testUnpublishLevelReturnsOk() {
            try (MockedStatic<AuthUtils> mockedAuth = Mockito.mockStatic(AuthUtils.class)) {
                mockedAuth.when(() -> AuthUtils.getUserIdFromAuth(Mockito.any()))
                        .thenReturn(USER_ID);
                Mockito.when(levelService.unpublishLevel(Mockito.eq(USER_ID), Mockito.eq(LEVEL_ID)))
                        .thenReturn(testLevel);

                final var status = restTestClient.put()
                        .uri("/levels/{levelId}/unpublish", LEVEL_ID)
                        .exchange()
                        .returnResult(LevelDTO.class)
                        .getStatus();
                Assertions.assertEquals(org.springframework.http.HttpStatus.OK, status);
            }
        }
    }

    /** Tests for PUT /levels/{levelId}/publish. */
    @Nested
    @DisplayName("PUT /levels/{levelId}/publish")
    /* default */ class PublishLevel {

        // Verifies that publishing a level returns 200 OK.
        @Test
        @DisplayName("should return 200 OK with published level")
        void testPublishLevelReturnsOk() {
            try (MockedStatic<AuthUtils> mockedAuth = Mockito.mockStatic(AuthUtils.class)) {
                mockedAuth.when(() -> AuthUtils.getUserIdFromAuth(Mockito.any()))
                        .thenReturn(USER_ID);
                Mockito.when(levelService.publish(Mockito.eq(USER_ID), Mockito.eq(LEVEL_ID)))
                        .thenReturn(testLevel);

                final var status = restTestClient.put()
                        .uri("/levels/{levelId}/publish", LEVEL_ID)
                        .exchange()
                        .returnResult(LevelDTO.class)
                        .getStatus();
                Assertions.assertEquals(org.springframework.http.HttpStatus.OK, status);
            }
        }
    }

    /** Tests for POST /levels/{levelId}/submit. */
    @Nested
    @DisplayName("POST /levels/{levelId}/submit")
    /* default */ class SubmitAttempt {

        // Verifies that submitting an attempt returns 200 OK.
        @Test
        @DisplayName("should return 200 OK with attempt result")
        void testSubmitAttemptReturnsOk() {
            try (MockedStatic<AuthUtils> mockedAuth = Mockito.mockStatic(AuthUtils.class)) {
                mockedAuth.when(() -> AuthUtils.getUserIdFromAuth(Mockito.any()))
                        .thenReturn(USER_ID);

                final AttemptDTO attemptDTO = new AttemptDTO(
                        Map.of(), new Position(0, 0), FIXED_TIMESTAMP, FIXED_DURATION, true);
                Mockito.when(levelService.submitAttempt(
                        Mockito.eq(LEVEL_ID), Mockito.eq(USER_ID), Mockito.any(AttemptDTO.class)))
                        .thenReturn("attempt-result");

                final var status = restTestClient.post()
                        .uri("/levels/{levelId}/submit", LEVEL_ID)
                        .body(attemptDTO)
                        .exchange()
                        .returnResult(String.class)
                        .getStatus();
                Assertions.assertEquals(org.springframework.http.HttpStatus.OK, status);
            }
        }
    }

    /** Tests for GET /levels/play/{levelId}/map. */
    @Nested
    @DisplayName("GET /levels/play/{levelId}/map")
    /* default */ class GetPlayableMap {

        // Verifies that getting a playable map returns 200 OK.
        @Test
        @DisplayName("should return 200 OK with map data")
        void testGetMapReturnsOk() {
            try (MockedStatic<AuthUtils> mockedAuth = Mockito.mockStatic(AuthUtils.class)) {
                mockedAuth.when(() -> AuthUtils.getUserIdFromAuth(Mockito.any()))
                        .thenReturn(USER_ID);
                Mockito.when(userService.getById(USER_ID))
                        .thenReturn(Optional.of(testUser));
                final Map<String, Object> mapData = Map.of("width", 16, "height", 16);
                Mockito.when(levelService.getPlayableMap(Mockito.eq(testUser), Mockito.eq(LEVEL_ID)))
                        .thenReturn(mapData);

                final var status = restTestClient.get()
                        .uri("/levels/play/{levelId}/map", LEVEL_ID)
                        .exchange()
                        .returnResult(Object.class)
                        .getStatus();
                Assertions.assertEquals(org.springframework.http.HttpStatus.OK, status);
            }
        }
    }
}