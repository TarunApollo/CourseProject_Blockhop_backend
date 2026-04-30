package ch.usi.inf.bsc.sa4.lab02spring.controller.level;

import ch.usi.inf.bsc.sa4.lab02spring.configuration.ControllerSecurityTestConfig;
import ch.usi.inf.bsc.sa4.lab02spring.configuration.ControllerSecurityTestSupport;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.AttemptDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.Position;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.service.UserService;
import ch.usi.inf.bsc.sa4.lab02spring.service.level.LevelPlayService;
import ch.usi.inf.bsc.sa4.lab02spring.utils.ForbiddenLevelActionException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.ForbiddenUserException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.LevelNotFoundException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.UserNotFoundException;

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

import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;

/// Black-box tests for [LevelPlayController] endpoints.
/// Verifies playable map retrieval and attempt submission via the real security filter chain.
@WebMvcTest(controllers = LevelPlayController.class)
@AutoConfigureRestTestClient
@Import(ControllerSecurityTestConfig.class)
@DisplayName("The Level Play Controller")
@SuppressWarnings("PMD.UnitTestShouldIncludeAssert")
class LevelPlayControllerTests {

    /// The authenticated user ID used across tests.
    private static final String USER_ID = "user-1";

    /// The authenticated user's display name.
    private static final String USER_NAME = "Test User";

    /// A level ID used across tests.
    private static final String LEVEL_ID = "level-1";

    /// A fixed attempt payload used across submit tests.
    private static final AttemptDTO ATTEMPT_DTO = new AttemptDTO(
            Map.of(), new Position(0, 0),
            ZonedDateTime.of(2025, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC),
            Duration.ofSeconds(30), false);

    /// Mocked play service.
    @MockitoBean
    private LevelPlayService levelPlayService;

    /// Mocked user service.
    @MockitoBean
    private UserService userService;

    /// Mocked decoder used by the resource-server security filter.
    @MockitoBean
    private JwtDecoder jwtDecoder;

    /// Client for performing REST calls.
    @Autowired
    private RestTestClient restTestClient;

    /// Shared test user instance.
    private static User testUser;

    /// Initializes static test data.
    @BeforeAll
    static void setupData() {
        testUser = new User(USER_ID, USER_NAME);
    }

    /// Configures the mocked JWT decoder before each test.
    @BeforeEach
    void setupJwt() {
        ControllerSecurityTestSupport.mockJwtDecoder(this.jwtDecoder, USER_ID, USER_NAME);
    }

    /// Tests for POST /levels/{levelId}/submit.
    @Nested
    @DisplayName("POST /levels/{levelId}/submit")
    class SubmitAttempt {

        /// Verifies that a valid attempt submission returns 200 OK with the result.
        @Test
        @DisplayName("should return 200 OK when the attempt is valid")
        void testSubmitAttemptSuccess() {
            Mockito.when(levelPlayService.handleLevelSubmission(
                    ArgumentMatchers.eq(LEVEL_ID),
                    ArgumentMatchers.eq(USER_ID),
                    ArgumentMatchers.any(AttemptDTO.class))).thenReturn("success");

            ControllerSecurityTestSupport
                    .withAuthAndCsrf(restTestClient.post().uri("/levels/{levelId}/submit", LEVEL_ID))
                    .body(ATTEMPT_DTO)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(String.class)
                    .isEqualTo("success");
        }

        /// Verifies that 404 is returned when the authenticated user does not exist.
        @Test
        @DisplayName("should return 404 Not Found when the user does not exist")
        void testSubmitAttemptUserNotFound() {
            Mockito.when(levelPlayService.handleLevelSubmission(
                    ArgumentMatchers.eq(LEVEL_ID),
                    ArgumentMatchers.eq(USER_ID),
                    ArgumentMatchers.any(AttemptDTO.class)))
                    .thenThrow(new UserNotFoundException());

            ControllerSecurityTestSupport
                    .withAuthAndCsrf(restTestClient.post().uri("/levels/{levelId}/submit", LEVEL_ID))
                    .body(ATTEMPT_DTO)
                    .exchange()
                    .expectStatus().isNotFound();
        }

        /// Verifies that 404 is returned when the requested level does not exist.
        @Test
        @DisplayName("should return 404 Not Found when the level does not exist")
        void testSubmitAttemptLevelNotFound() {
            Mockito.when(levelPlayService.handleLevelSubmission(
                    ArgumentMatchers.eq(LEVEL_ID),
                    ArgumentMatchers.eq(USER_ID),
                    ArgumentMatchers.any(AttemptDTO.class)))
                    .thenThrow(new LevelNotFoundException());

            ControllerSecurityTestSupport
                    .withAuthAndCsrf(restTestClient.post().uri("/levels/{levelId}/submit", LEVEL_ID))
                    .body(ATTEMPT_DTO)
                    .exchange()
                    .expectStatus().isNotFound();
        }

        /// Verifies that 403 is returned when the user attempts a forbidden level action.
        @Test
        @DisplayName("should return 403 Forbidden when the level action is not permitted")
        void testSubmitAttemptForbiddenLevelAction() {
            Mockito.when(levelPlayService.handleLevelSubmission(
                    ArgumentMatchers.eq(LEVEL_ID),
                    ArgumentMatchers.eq(USER_ID),
                    ArgumentMatchers.any(AttemptDTO.class)))
                    .thenThrow(new ForbiddenLevelActionException(""));

            ControllerSecurityTestSupport
                    .withAuthAndCsrf(restTestClient.post().uri("/levels/{levelId}/submit", LEVEL_ID))
                    .body(ATTEMPT_DTO)
                    .exchange()
                    .expectStatus().isForbidden();
        }

        /// Verifies that 403 is returned when the user is not authorized for the action.
        @Test
        @DisplayName("should return 403 Forbidden when the user is not authorized")
        void testSubmitAttemptForbiddenUser() {
            Mockito.when(levelPlayService.handleLevelSubmission(
                    ArgumentMatchers.eq(LEVEL_ID),
                    ArgumentMatchers.eq(USER_ID),
                    ArgumentMatchers.any(AttemptDTO.class)))
                    .thenThrow(new ForbiddenUserException(""));

            ControllerSecurityTestSupport
                    .withAuthAndCsrf(restTestClient.post().uri("/levels/{levelId}/submit", LEVEL_ID))
                    .body(ATTEMPT_DTO)
                    .exchange()
                    .expectStatus().isForbidden();
        }
    }

    /// Tests for GET /levels/play/{levelId}/map.
    @Nested
    @DisplayName("GET /levels/play/{levelId}/map")
    class GetMap {

        /// Verifies that 200 OK and the playable map are returned for a valid user and level.
        @Test
        @DisplayName("should return 200 OK and the playable map")
        void testGetMapSuccess() {
            final Map<String, Object> expectedMap = Map.of("key", "value");
            Mockito.when(userService.getById(USER_ID)).thenReturn(Optional.of(testUser));
            Mockito.when(levelPlayService.getPlayableMap(testUser, LEVEL_ID)).thenReturn(expectedMap);

            ControllerSecurityTestSupport
                    .withAuthAndCsrf(restTestClient.get().uri("/levels/play/{levelId}/map", LEVEL_ID))
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .isEqualTo(expectedMap);
        }

        /// Verifies that 404 is returned when the authenticated user does not exist.
        @Test
        @DisplayName("should return 404 Not Found when the user does not exist")
        void testGetMapUserNotFound() {
            Mockito.when(userService.getById(USER_ID)).thenReturn(Optional.empty());

            ControllerSecurityTestSupport
                    .withAuthAndCsrf(restTestClient.get().uri("/levels/play/{levelId}/map", LEVEL_ID))
                    .exchange()
                    .expectStatus().isNotFound();
        }
    }
}