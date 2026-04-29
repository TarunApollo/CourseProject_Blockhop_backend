package ch.usi.inf.bsc.sa4.lab02spring.controller.level;

import ch.usi.inf.bsc.sa4.lab02spring.configuration.ControllerSecurityTestConfig;
import ch.usi.inf.bsc.sa4.lab02spring.configuration.ControllerSecurityTestSupport;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.CloneLevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.CreateLevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.LevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.UpdateLevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.ClearCondition;
import ch.usi.inf.bsc.sa4.lab02spring.model.Condition;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.service.UserService;
import ch.usi.inf.bsc.sa4.lab02spring.service.level.LevelService;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/// Black-box tests for `LevelController` CRUD endpoints.
///
/// The tests keep Spring Security filters active. Requests authenticate through
/// the resource-server filter using a bearer token decoded by a mocked
/// `JwtDecoder`, and unsafe HTTP methods include a CSRF cookie/header pair
/// accepted by `CookieCsrfTokenRepository`.
@WebMvcTest(controllers = LevelController.class, excludeAutoConfiguration = {
        OAuth2ClientAutoConfiguration.class
})
@AutoConfigureRestTestClient
@Import(ControllerSecurityTestConfig.class)
@DisplayName("Level Controller CRUD Logic Tests")
class LevelControllerTests {

    /// The fake authenticated user ID used across tests.
    private static final String USER_ID = "userid1";

    /// The fake authenticated user's display name.
    private static final String USER_NAME = "Test User";

    /// A fake level ID used across tests.
    private static final String LEVEL_ID = "level-1";

    /// Mocked service for core level operations.
    @MockitoBean
    private LevelService levelService;

    /// Mocked service for user resolution.
    @MockitoBean
    private UserService userService;

    /// Mocked decoder used by the resource-server security filter.
    @MockitoBean
    private JwtDecoder jwtDecoder;

    /// Client used to perform REST calls.
    @Autowired
    private RestTestClient restTestClient;

    /// Shared test user instance.
    private static User testUser;

    /// Shared test level instance.
    private static Level testLevel;

    /// Initializes static test data.
    @BeforeAll
    static void setupData() {
        testUser = new User(USER_ID, USER_NAME);
        testLevel = new Level("Title", "Desc", testUser);
    }

    /// Configures the mocked JWT decoder used by authenticated requests.
    @BeforeEach
    /* default */ void setupJwt() {
        ControllerSecurityTestSupport.mockJwtDecoder(
                this.jwtDecoder, USER_ID, USER_NAME);
    }

    /// Tests for POST /levels.
    @Nested
    @DisplayName("POST /levels")
    /* default */ class CreateLevel {

        /// Verifies that creating a level returns 200 OK.
        @Test
        @DisplayName("should return 200 OK")
        /* default */ void shouldReturnOk() {
            when(
                    levelService.createLevel(
                            any(CreateLevelDTO.class),
                            eq(USER_ID)))
                    .thenReturn(testLevel);

            final HttpStatusCode status = ControllerSecurityTestSupport
                    .withAuthAndCsrf(restTestClient.post().uri("/levels"))
                    .body(new CreateLevelDTO("T", "D"))
                    .exchange()
                    .returnResult(LevelDTO.class)
                    .getStatus();
            Assertions.assertEquals(HttpStatus.OK, status);
        }
    }

    /// Tests for POST /levels/clone.
    @Nested
    @DisplayName("POST /levels/clone")
    /* default */ class CloneLevel {

        /// Verifies that cloning a level returns 200 OK.
        @Test
        @DisplayName("should return 200 OK")
        /* default */ void shouldReturnOk() {
            when(userService.getById(USER_ID))
                    .thenReturn(Optional.of(testUser));
            when(levelService.cloneLevel(
                    any(CloneLevelDTO.class),
                    eq(testUser)))
                    .thenReturn(Optional.of(testLevel));

            final HttpStatusCode status = ControllerSecurityTestSupport
                    .withAuthAndCsrf(restTestClient.post().uri("/levels/clone"))
                    .body(new CloneLevelDTO(LEVEL_ID))
                    .exchange()
                    .returnResult(LevelDTO.class)
                    .getStatus();
            Assertions.assertEquals(HttpStatus.OK, status);
        }
    }

    /// Tests for PUT /levels/{id}/properties.
    @Nested
    @DisplayName("PUT /levels/{id}/properties")
    /* default */ class UpdateLevelProperties {

        /// Verifies that updating level properties returns 200 OK.
        @Test
        @DisplayName("should return 200 OK")
        /* default */ void shouldReturnOk() {
            when(userService.getById(USER_ID))
                    .thenReturn(Optional.of(testUser));
            when(levelService.updateLevelProperties(
                    eq(testUser),
                    eq(LEVEL_ID),
                    any()))
                    .thenReturn(testLevel);

            final UpdateLevelDTO dto = new UpdateLevelDTO(
                    Optional.of("U"), Optional.empty(),
                    Optional.of(new ClearCondition(
                            new Condition.NoClearCondition(), 0)));
            final HttpStatusCode status = ControllerSecurityTestSupport
                    .withAuthAndCsrf(restTestClient.put().uri("/levels/{id}/properties", LEVEL_ID))
                    .body(dto)
                    .exchange()
                    .returnResult(LevelDTO.class)
                    .getStatus();
            Assertions.assertEquals(HttpStatus.OK, status);
        }
    }

    /// Tests for DELETE /levels/{id}.
    @Nested
    @DisplayName("DELETE /levels/{id}")
    /* default */ class DeleteLevel {

        /// Verifies that deleting a level returns 204 No Content.
        @Test
        @DisplayName("should return 204 No Content")
        /* default */ void shouldReturnNoContent() {
            doNothing().when(levelService)
                    .deleteLevel(USER_ID, LEVEL_ID);

            final HttpStatusCode status = ControllerSecurityTestSupport
                    .withAuthAndCsrf(restTestClient.delete().uri("/levels/{id}", LEVEL_ID))
                    .exchange()
                    .returnResult(Void.class)
                    .getStatus();
            Assertions.assertEquals(HttpStatus.NO_CONTENT, status);
        }
    }
}
