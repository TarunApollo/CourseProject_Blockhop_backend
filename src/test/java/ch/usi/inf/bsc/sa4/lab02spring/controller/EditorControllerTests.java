package ch.usi.inf.bsc.sa4.lab02spring.controller;

import ch.usi.inf.bsc.sa4.lab02spring.configuration.ControllerSecurityTestConfig;
import ch.usi.inf.bsc.sa4.lab02spring.configuration.ControllerSecurityTestSupport;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.BoxPropertyUpdateDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.EditorLevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.ObjectLayerResponseDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.UpdateObjectLayerDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.UpdateObjectPropertiesDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.UpdateWorldLayerDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.WorldLayerResponseDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.Content;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.Position;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.service.EditorService;

import ch.usi.inf.bsc.sa4.lab02spring.utils.ForbiddenUserException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.LevelNotFoundException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.LevelPublishedException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.List;

/// Black-box tests for [EditorController]. Tests HTTP contract: status codes,
/// response bodies, and content types.
@WebMvcTest(controllers = EditorController.class)
@AutoConfigureRestTestClient
@Import(ControllerSecurityTestConfig.class)
@SuppressWarnings("PMD.UnitTestShouldIncludeAssert")
@DisplayName("The Editor Controller")
class EditorControllerTests {

    /// path test
    private static final String WORLD_LAYER_PATH = "/editor/{levelId}/world-layer";

    /// The authenticated user ID used across tests.
    private static final String USER_ID = "userid1";

    /// The authenticated user name used across tests.
    private static final String USER_NAME = "Test User";

    /// A level ID used across tests.
    private static final String LEVEL_ID = "level-1";

    /// The mocked editor service.
    @MockitoBean
    private EditorService editorService;

    /// Mocked decoder used by the resource-server security filter.
    @MockitoBean
    private JwtDecoder jwtDecoder;

    /// The RestTestClient for performing requests.
    @Autowired
    private RestTestClient restTestClient;

    /// A test level owned by the test user.
    private static Level testLevel;

    /// Initializes static test data.
    @BeforeAll
    static void setupData() {
        testLevel = new Level("Editor Level", "A description", new User(USER_ID, USER_NAME));
    }

    /// Configures the mocked JWT decoder and common service stubs.
    @BeforeEach
    void setup() {
        ControllerSecurityTestSupport.mockJwtDecoder(this.jwtDecoder, USER_ID, USER_NAME);
    }

    /// Tests for PUT /editor/{levelId}/world-layer.
    @Nested
    @DisplayName("PUT " + WORLD_LAYER_PATH)
    class ReplaceWorldLayer {

        /// Verifies that replacing the world layer returns 200 OK.
        @Test
        @DisplayName("should return 200 OK with updated world layer")
        void testReplaceWorldLayerReturnsOk() {
            Mockito.when(editorService.replaceWorldLayer(
                    Mockito.eq(USER_ID),
                    Mockito.eq(LEVEL_ID),
                    Mockito.any(UpdateWorldLayerDTO.class)))
                    .thenReturn(testLevel);

            final UpdateWorldLayerDTO dto = new UpdateWorldLayerDTO(
                    List.of(EditorLevelDTO.create(new Position(0, 0), 1)));

            ControllerSecurityTestSupport.withAuthAndCsrf(restTestClient.put()
                    .uri(WORLD_LAYER_PATH, LEVEL_ID))
                    .body(dto)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(WorldLayerResponseDTO.class);
        }

        /// Verifies that replacing the world layer returns 404 if the level is
        /// not found.
        @Test
        @DisplayName("should return 404 Not Found when level does not exist")
        void testReplaceWorldLayerReturnsNotFound() {
            Mockito.when(editorService.replaceWorldLayer(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
                    .thenThrow(new LevelNotFoundException());

            final UpdateWorldLayerDTO dto = new UpdateWorldLayerDTO(List.of());

            ControllerSecurityTestSupport.withAuthAndCsrf(restTestClient.put()
                    .uri(WORLD_LAYER_PATH, "invalid-id"))
                    .body(dto)
                    .exchange()
                    .expectStatus().isNotFound();
        }

        /// Verifies that replacing the world layer returns 403 if the user is
        /// not authorized.
        @Test
        @DisplayName("should return 403 Forbidden when user is not authorized")
        void testReplaceWorldLayerReturnsForbidden() {
            Mockito.when(editorService.replaceWorldLayer(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
                    .thenThrow(new ForbiddenUserException("Not owner"));

            final UpdateWorldLayerDTO dto = new UpdateWorldLayerDTO(List.of());

            ControllerSecurityTestSupport.withAuthAndCsrf(restTestClient.put()
                    .uri(WORLD_LAYER_PATH, LEVEL_ID))
                    .body(dto)
                    .exchange()
                    .expectStatus().isForbidden();
        }

        /// Verifies that replacing the world layer returns 400 if arguments
        /// are invalid.
        @Test
        @DisplayName("should return 400 Bad Request when arguments are invalid")
        void testReplaceWorldLayerReturnsBadRequest() {
            Mockito.when(editorService.replaceWorldLayer(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
                    .thenThrow(new IllegalArgumentException("Invalid bounds"));

            final UpdateWorldLayerDTO dto = new UpdateWorldLayerDTO(List.of());

            ControllerSecurityTestSupport.withAuthAndCsrf(restTestClient.put()
                    .uri(WORLD_LAYER_PATH, LEVEL_ID))
                    .body(dto)
                    .exchange()
                    .expectStatus().isBadRequest();
        }
    }

    /// Tests for PUT /editor/{levelId}/object-layer.
    @Nested
    @DisplayName("PUT /editor/{levelId}/object-layer")
    class ReplaceObjectLayer {

        /// Verifies that replacing the object layer returns 200 OK.
        @Test
        @DisplayName("should return 200 OK with updated object layer")
        void testReplaceObjectLayerReturnsOk() {
            Mockito.when(editorService.replaceObjectLayer(
                    Mockito.eq(USER_ID),
                    Mockito.eq(LEVEL_ID),
                    Mockito.any(UpdateObjectLayerDTO.class)))
                    .thenReturn(testLevel);

            final UpdateObjectLayerDTO dto = new UpdateObjectLayerDTO(
                    List.of(EditorLevelDTO.create(new Position(1, 1), 42)));

            ControllerSecurityTestSupport.withAuthAndCsrf(restTestClient.put()
                    .uri("/editor/{levelId}/object-layer", LEVEL_ID))
                    .body(dto)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(ObjectLayerResponseDTO.class);
        }

        /// Verifies that replacing the object layer returns 403 if the level
        /// is published.
        @Test
        @DisplayName("should return 403 Forbidden when level is published")
        void testReplaceObjectLayerReturnsForbidden() {
            Mockito.when(editorService.replaceObjectLayer(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
                    .thenThrow(new LevelPublishedException("Published"));

            final UpdateObjectLayerDTO dto = new UpdateObjectLayerDTO(List.of());

            ControllerSecurityTestSupport.withAuthAndCsrf(restTestClient.put()
                    .uri("/editor/{levelId}/object-layer", LEVEL_ID))
                    .body(dto)
                    .exchange()
                    .expectStatus().isForbidden();
        }
    }

    /// Tests for PATCH /editor/{levelId}/object-layer/properties.
    @Nested
    @DisplayName("PATCH /editor/{levelId}/object-layer/properties")
    class UpdateObjectProperties {

        /// Verifies that updating object properties returns 200 OK.
        @Test
        @DisplayName("should return 200 OK with updated object layer")
        void testUpdateObjectPropertiesReturnsOk() {
            Mockito.when(editorService.updateObjectProperties(
                    Mockito.eq(USER_ID),
                    Mockito.eq(LEVEL_ID),
                    Mockito.any(UpdateObjectPropertiesDTO.class)))
                    .thenReturn(testLevel);

            final UpdateObjectPropertiesDTO dto = new BoxPropertyUpdateDTO(
                    new Position(2, 2), new Content.NoContent());

            ControllerSecurityTestSupport.withAuthAndCsrf(restTestClient.patch()
                    .uri("/editor/{levelId}/object-layer/properties", LEVEL_ID))
                    .body(dto)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(ObjectLayerResponseDTO.class);
        }

        /// Verifies that updating object properties returns 400 if properties
        /// are invalid.
        @Test
        @DisplayName("should return 400 Bad Request when properties are invalid")
        void testUpdateObjectPropertiesReturnsBadRequest() {
            Mockito.when(editorService.updateObjectProperties(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
                    .thenThrow(new IllegalArgumentException("Invalid content"));

            final UpdateObjectPropertiesDTO dto = new BoxPropertyUpdateDTO(
                    new Position(2, 2), new Content.NoContent());

            ControllerSecurityTestSupport.withAuthAndCsrf(restTestClient.patch()
                    .uri("/editor/{levelId}/object-layer/properties", LEVEL_ID))
                    .body(dto)
                    .exchange()
                    .expectStatus().isBadRequest();
        }
    }
}
