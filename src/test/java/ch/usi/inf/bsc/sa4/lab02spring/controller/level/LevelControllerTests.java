package ch.usi.inf.bsc.sa4.lab02spring.controller.level;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.CloneLevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.CreateLevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.LevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.UpdateLevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.SetLevelAttitudeDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.LevelAttitudeType;
import ch.usi.inf.bsc.sa4.lab02spring.model.ClearCondition;
import ch.usi.inf.bsc.sa4.lab02spring.model.Condition;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.repository.LevelRepository;
import ch.usi.inf.bsc.sa4.lab02spring.service.UserService;
import ch.usi.inf.bsc.sa4.lab02spring.service.level.LevelService;
import ch.usi.inf.bsc.sa4.lab02spring.service.level.LevelAttitudeService;
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

import java.util.Optional;

/// Black-box tests for LevelController (CRUD). Follows the pattern from the
/// original LevelControllerLogicTests.
///
@WebMvcTest(controllers = LevelController.class, excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        OAuth2ClientAutoConfiguration.class,
        OAuth2ResourceServerAutoConfiguration.class
})
@AutoConfigureRestTestClient
@DisplayName("Level Controller CRUD Logic Tests")
@SuppressWarnings("PMD.ExcessiveImports") // justification: the imports are necessary for mocking and testing
class LevelControllerTests {

    /// The authenticated user ID used across tests.
    private static final String USER_ID = "userid1";

    /// A level ID used across tests.
    private static final String LEVEL_ID = "level-1";

    /// Mocked service for core level operations.
    @MockitoBean
    private LevelService levelService;

    /// Mocked service for user resolution.
    @MockitoBean
    private UserService userService;

    /// Mocked service for level attitudes.
    @MockitoBean
    private LevelAttitudeService levelAttitudeService;

    /// Mocked repository for level data.
    @MockitoBean
    private LevelRepository levelRepository;

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
        testUser = new User(USER_ID, "Test User");
        testLevel = new Level("Title", "Desc", testUser);
    }

    /// Verifies that creating a level returns 200 OK.
    @Test
    @DisplayName("POST /levels should return 200 OK")
    void testCreateLevel() {
        try (MockedStatic<AuthUtils> mockedAuth = Mockito.mockStatic(AuthUtils.class)) {
            mockedAuth.when(() -> AuthUtils.getUserIdFromAuth(Mockito.any())).thenReturn(USER_ID);
            Mockito.when(levelService.createLevel(Mockito.any(CreateLevelDTO.class), Mockito.eq(USER_ID)))
                    .thenReturn(testLevel);

            final HttpStatusCode status = restTestClient.post().uri("/levels")
                    .body(new CreateLevelDTO("T", "D"))
                    .exchange()
                    .returnResult(LevelDTO.class)
                    .getStatus();
            Assertions.assertEquals(HttpStatus.OK, status);
        }
    }

    /// Verifies that cloning a level returns 200 OK.
    @Test
    @DisplayName("POST /levels/clone should return 200 OK")
    void testCloneLevel() {
        try (MockedStatic<AuthUtils> mockedAuth = Mockito.mockStatic(AuthUtils.class)) {
            mockedAuth.when(() -> AuthUtils.getUserIdFromAuth(Mockito.any())).thenReturn(USER_ID);
            Mockito.when(userService.getById(USER_ID)).thenReturn(Optional.of(testUser));
            Mockito.when(levelService.cloneLevel(Mockito.any(CloneLevelDTO.class), Mockito.eq(testUser)))
                    .thenReturn(Optional.of(testLevel));

            final HttpStatusCode status = restTestClient.post().uri("/levels/clone")
                    .body(new CloneLevelDTO(LEVEL_ID))
                    .exchange()
                    .returnResult(LevelDTO.class)
                    .getStatus();
            Assertions.assertEquals(HttpStatus.OK, status);
        }
    }

    /// Verifies that updating level properties returns 200 OK.
    @Test
    @DisplayName("PUT /levels/{id}/properties should return 200 OK")
    void testUpdateLevelProperties() {
        try (MockedStatic<AuthUtils> mockedAuth = Mockito.mockStatic(AuthUtils.class)) {
            mockedAuth.when(() -> AuthUtils.getUserIdFromAuth(Mockito.any())).thenReturn(USER_ID);
            Mockito.when(userService.getById(USER_ID)).thenReturn(Optional.of(testUser));
            Mockito.when(levelService.updateLevelProperties(Mockito.eq(testUser), Mockito.eq(LEVEL_ID), Mockito.any()))
                    .thenReturn(testLevel);

            final UpdateLevelDTO dto = new UpdateLevelDTO(Optional.of("U"), Optional.empty(),
                    Optional.of(new ClearCondition(new Condition.NoClearCondition(), 0)));
            final HttpStatusCode status = restTestClient.put().uri("/levels/{id}/properties", LEVEL_ID)
                    .body(dto)
                    .exchange()
                    .returnResult(LevelDTO.class)
                    .getStatus();
            Assertions.assertEquals(HttpStatus.OK, status);
        }
    }

    /// Verifies that setting an attitude returns 200 OK.
    @Test
    @DisplayName("PUT /levels/{id}/attitude should return 200 OK")
    void testUpdateLevelAttitude() {
        try (MockedStatic<AuthUtils> mockedAuth = Mockito.mockStatic(AuthUtils.class)) {
            mockedAuth.when(() -> AuthUtils.getUserIdFromAuth(Mockito.any())).thenReturn(USER_ID);
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(testLevel));

            final HttpStatusCode status = restTestClient.put().uri("/levels/{id}/attitude", LEVEL_ID)
                    .body(new SetLevelAttitudeDTO(LevelAttitudeType.LIKE))
                    .exchange()
                    .returnResult(Void.class)
                    .getStatus();
            Assertions.assertEquals(HttpStatus.OK, status);
        }
    }

    /// Verifies that deleting an attitude returns 204 No Content.
    @Test
    @DisplayName("DELETE /levels/{id}/attitude should return 204 No Content")
    void testDeleteLevelAttitude() {
        try (MockedStatic<AuthUtils> mockedAuth = Mockito.mockStatic(AuthUtils.class)) {
            mockedAuth.when(() -> AuthUtils.getUserIdFromAuth(Mockito.any())).thenReturn(USER_ID);
            Mockito.doNothing().when(levelAttitudeService).deleteAttitude(USER_ID, LEVEL_ID);

            final HttpStatusCode status = restTestClient.delete().uri("/levels/{id}/attitude", LEVEL_ID)
                    .exchange()
                    .returnResult(Void.class)
                    .getStatus();
            Assertions.assertEquals(HttpStatus.NO_CONTENT, status);
        }
    }
}
