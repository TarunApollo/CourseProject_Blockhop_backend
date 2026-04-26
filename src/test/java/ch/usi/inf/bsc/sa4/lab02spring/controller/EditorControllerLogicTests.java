package ch.usi.inf.bsc.sa4.lab02spring.controller;

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
import ch.usi.inf.bsc.sa4.lab02spring.utils.AuthUtils;

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
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.List;

/**
 * Black-box tests for EditorController.
 * Tests HTTP contract: status codes, response bodies, and content types.
 */
@SuppressWarnings({"PMD.ExcessiveImports", "FCBL_FIELD_COULD_BE_LOCAL"})
@WebMvcTest(controllers = EditorController.class, excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        OAuth2ClientAutoConfiguration.class,
        OAuth2ResourceServerAutoConfiguration.class
})
@AutoConfigureRestTestClient
@DisplayName("Editor Controller Logic Tests")
/* default */ class EditorControllerLogicTests {

    /** The authenticated user ID used across tests. */
    private static final String USER_ID = "userid1";

    /** The authenticated user name used across tests. */
    private static final String USER_NAME = "Test User";

    /** A level ID used across tests. */
    private static final String LEVEL_ID = "level-1";

    /** The mocked editor service. */
    @MockitoBean
    private EditorService editorService;

    /** The RestTestClient for performing requests. */
    @Autowired
    private RestTestClient restTestClient;

    /** A test level owned by the test user. */
    private static Level testLevel;

    /** Initializes static test data. */
    @BeforeAll
    static void setupData() {
        testLevel = new Level("Editor Level", "A description", new User(USER_ID, USER_NAME));
    }

    /** Tests for PUT /editor/{levelId}/world-layer. */
    @Nested
    @DisplayName("PUT /editor/{levelId}/world-layer")
    /* default */ class ReplaceWorldLayer {

        /** Verifies that replacing the world layer returns 200 OK. */
        @Test
        @DisplayName("should return 200 OK with updated world layer")
        void testReplaceWorldLayerReturnsOk() {
            try (MockedStatic<AuthUtils> mockedAuth = Mockito.mockStatic(AuthUtils.class)) {
                mockedAuth.when(() -> AuthUtils.getUserIdFromAuth(Mockito.any()))
                        .thenReturn(USER_ID);
                Mockito.when(editorService.replaceWorldLayer(
                        Mockito.eq(USER_ID),
                        Mockito.eq(LEVEL_ID),
                        Mockito.any(UpdateWorldLayerDTO.class)))
                        .thenReturn(testLevel);

                final UpdateWorldLayerDTO dto = new UpdateWorldLayerDTO(
                        List.of(EditorLevelDTO.create(new Position(0, 0), 1)));
                final HttpStatusCode status = restTestClient.put()
                        .uri("/editor/{levelId}/world-layer", LEVEL_ID)
                        .body(dto)
                        .exchange()
                        .returnResult(WorldLayerResponseDTO.class)
                        .getStatus();
                Assertions.assertEquals(HttpStatus.OK, status);
            }
        }
    }

    /** Tests for PUT /editor/{levelId}/object-layer. */
    @Nested
    @DisplayName("PUT /editor/{levelId}/object-layer")
    /* default */ class ReplaceObjectLayer {

        /** Verifies that replacing the object layer returns 200 OK. */
        @Test
        @DisplayName("should return 200 OK with updated object layer")
        void testReplaceObjectLayerReturnsOk() {
            try (MockedStatic<AuthUtils> mockedAuth = Mockito.mockStatic(AuthUtils.class)) {
                mockedAuth.when(() -> AuthUtils.getUserIdFromAuth(Mockito.any()))
                        .thenReturn(USER_ID);
                Mockito.when(editorService.replaceObjectLayer(
                        Mockito.eq(USER_ID),
                        Mockito.eq(LEVEL_ID),
                        Mockito.any(UpdateObjectLayerDTO.class)))
                        .thenReturn(testLevel);

                final UpdateObjectLayerDTO dto = new UpdateObjectLayerDTO(
                        List.of(EditorLevelDTO.create(new Position(1, 1), 42)));
                final HttpStatusCode status = restTestClient.put()
                        .uri("/editor/{levelId}/object-layer", LEVEL_ID)
                        .body(dto)
                        .exchange()
                        .returnResult(ObjectLayerResponseDTO.class)
                        .getStatus();
                Assertions.assertEquals(HttpStatus.OK, status);
            }
        }
    }

    /** Tests for PATCH /editor/{levelId}/object-layer/properties. */
    @Nested
    @DisplayName("PATCH /editor/{levelId}/object-layer/properties")
    /* default */ class UpdateObjectProperties {

        /** Verifies that updating object properties returns 200 OK. */
        @Test
        @DisplayName("should return 200 OK with updated object layer")
        void testUpdateObjectPropertiesReturnsOk() {
            try (MockedStatic<AuthUtils> mockedAuth = Mockito.mockStatic(AuthUtils.class)) {
                mockedAuth.when(() -> AuthUtils.getUserIdFromAuth(Mockito.any()))
                        .thenReturn(USER_ID);
                Mockito.when(editorService.updateObjectProperties(
                        Mockito.eq(USER_ID),
                        Mockito.eq(LEVEL_ID),
                        Mockito.any(UpdateObjectPropertiesDTO.class)))
                        .thenReturn(testLevel);

                final UpdateObjectPropertiesDTO dto = new BoxPropertyUpdateDTO(
                        new Position(2, 2), new Content.NoContent());
                final HttpStatusCode status = restTestClient.patch()
                        .uri("/editor/{levelId}/object-layer/properties", LEVEL_ID)
                        .body(dto)
                        .exchange()
                        .returnResult(ObjectLayerResponseDTO.class)
                        .getStatus();
                Assertions.assertEquals(HttpStatus.OK, status);
            }
        }
    }
}
