package ch.usi.inf.bsc.sa4.lab02spring.controller.level;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.AttemptDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.Position;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.service.UserService;
import ch.usi.inf.bsc.sa4.lab02spring.service.level.LevelPlayService;
import ch.usi.inf.bsc.sa4.lab02spring.utils.AuthUtils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
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

import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;

///
 /// Black-box tests for LevelPlayController.
 /// Verifies endpoints for playable map retrieval and attempt submission.
 ///
@WebMvcTest(controllers = LevelPlayController.class, excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        OAuth2ClientAutoConfiguration.class,
        OAuth2ResourceServerAutoConfiguration.class
})
@AutoConfigureRestTestClient
@DisplayName("Level Play Controller Logic Tests")
class LevelPlayControllerTests {

    /// The authenticated user ID used across tests.
    private static final String USER_ID = "userid1";

    /// A level ID used across tests.
    private static final String LEVEL_ID = "level-1";

    /// A fixed timestamp used for attempt tests.
    private static final ZonedDateTime FIXED_TIMESTAMP =
            ZonedDateTime.of(2025, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

    /// A fixed duration used for attempt tests.
    private static final Duration FIXED_DURATION = Duration.ofSeconds(30);

    /// The mocked play service.
    @MockitoBean
    private LevelPlayService levelPlayService;

    /// The mocked user service.
    @MockitoBean
    private UserService userService;

    /// The RestTestClient for performing requests.
    @Autowired
    private RestTestClient restTestClient;

    /// Shared test user instance.
    private static User testUser;

    /// Initializes static test data. ///
    @BeforeAll
    static void setupData() {
        testUser = new User(USER_ID, "Test User");
    }

    /// Verifies that submitting an attempt returns 200 OK. ///
    @Test
    @DisplayName("POST /levels/{id}/submit should return 200 OK")
    void testSubmitAttempt() {
        try (MockedStatic<AuthUtils> mockedAuth = Mockito.mockStatic(AuthUtils.class)) {
            mockedAuth.when(() -> AuthUtils.getUserIdFromAuth(Mockito.any())).thenReturn(USER_ID);

            final AttemptDTO dto = new AttemptDTO(Map.of(), new Position(0, 0), 
                    FIXED_TIMESTAMP, FIXED_DURATION, true);
            
            Mockito.when(levelPlayService.handleLevelSubmission(LEVEL_ID, USER_ID, dto))
                    .thenReturn("success");

            final HttpStatusCode status = restTestClient.post()
                    .uri("/levels/{levelId}/submit", LEVEL_ID)
                    .body(dto)
                    .exchange()
                    .returnResult(String.class)
                    .getStatus();
            Assertions.assertEquals(HttpStatus.OK, status);
        }
    }

    /// Verifies that getting a playable map returns 200 OK. ///
    @Test
    @DisplayName("GET /levels/play/{id}/map should return 200 OK")
    void testGetMap() {
        try (MockedStatic<AuthUtils> mockedAuth = Mockito.mockStatic(AuthUtils.class)) {
            mockedAuth.when(() -> AuthUtils.getUserIdFromAuth(Mockito.any())).thenReturn(USER_ID);
            Mockito.when(userService.getById(USER_ID)).thenReturn(Optional.of(testUser));
            Mockito.when(levelPlayService.getPlayableMap(testUser, LEVEL_ID)).thenReturn(Map.of());

            final HttpStatusCode status = restTestClient.get()
                    .uri("/levels/play/{levelId}/map", LEVEL_ID)
                    .exchange()
                    .returnResult(Object.class)
                    .getStatus();
            Assertions.assertEquals(HttpStatus.OK, status);
        }
    }
}
