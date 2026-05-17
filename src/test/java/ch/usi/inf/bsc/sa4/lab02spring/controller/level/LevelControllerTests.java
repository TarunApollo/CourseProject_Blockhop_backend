package ch.usi.inf.bsc.sa4.lab02spring.controller.level;

import ch.usi.inf.bsc.sa4.lab02spring.configuration.ControllerSecurityTestConfig;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.CloneLevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.CreateLevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.LevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.UpdateLevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.SetLevelAttitudeDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.LevelAttitudeType;
import ch.usi.inf.bsc.sa4.lab02spring.model.ClearCondition;
import ch.usi.inf.bsc.sa4.lab02spring.model.ClearConditionType;
import ch.usi.inf.bsc.sa4.lab02spring.model.Condition;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.service.UserService;
import ch.usi.inf.bsc.sa4.lab02spring.service.level.LevelService;
import ch.usi.inf.bsc.sa4.lab02spring.utils.ForbiddenUserException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.LevelNotFoundException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.UserNotFoundException;
import ch.usi.inf.bsc.sa4.lab02spring.service.level.LevelAttitudeService;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.Optional;

/// Black-box tests for [LevelController] CRUD endpoints.
@SpringBootTest
@AutoConfigureMockMvc
@Import(ControllerSecurityTestConfig.class)
@SuppressWarnings({ "PMD.UnitTestShouldIncludeAssert", "PMD.ExcessiveImports" })
@DisplayName("The Level Controller")
class LevelControllerTests {

    /// A fake level ID used across tests.
    private static final String LEVEL_ID = "level-1";

    /// URI template for the level properties endpoint.
    private static final String PROPERTIES_URI = "/levels/{id}/properties";

    /// Mocked service for core level operations.
    @MockitoBean
    private LevelService levelService;

    /// Mocked service for user resolution.
    @MockitoBean
    private UserService userService;

    /// Mocked service for level attitudes.
    @MockitoBean
    private LevelAttitudeService levelAttitudeService;

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
        testUser = new User(ControllerSecurityTestConfig.DEFAULT_USER_ID,
                ControllerSecurityTestConfig.DEFAULT_USER_NAME);
        testLevel = new Level("Title", "Desc", testUser);
    }

    /// Tests for POST /levels.
    @Nested
    @DisplayName("POST /levels")
    class CreateLevel {

        /// Verifies that creating a level returns 200 OK and the level DTO.
        @Test
        @DisplayName("should return 200 OK and the created level")
        void shouldReturnOk() {
            Mockito.when(
                    levelService.createLevel(
                            Mockito.any(CreateLevelDTO.class),
                            Mockito.eq(ControllerSecurityTestConfig.DEFAULT_USER_ID)))
                    .thenReturn(testLevel);

            restTestClient.post().uri("/levels")
                    .body(new CreateLevelDTO("T", "D",
                            new ClearCondition(new Condition.NoClearCondition(), 0)))
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(LevelDTO.class)
                    .isEqualTo(new LevelDTO(testLevel));
        }

        /// Verifies that creation accepts a clear condition in the request body.
        @Test
        @DisplayName("should accept a clear condition when creating a level")
        void shouldAcceptClearCondition() {
            final ClearCondition clearCondition = new ClearCondition(
                    new Condition.SomeClearCondition(ClearConditionType.COIN), 3);
            final Level levelWithCondition = new Level("Title", "Desc", testUser);
            levelWithCondition.setClearCondition(clearCondition);

            Mockito.when(
                    levelService.createLevel(
                            Mockito.any(CreateLevelDTO.class),
                            Mockito.eq(ControllerSecurityTestConfig.DEFAULT_USER_ID)))
                    .thenReturn(levelWithCondition);

            restTestClient.post().uri("/levels")
                    .body(new CreateLevelDTO("T", "D", clearCondition))
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(LevelDTO.class)
                    .isEqualTo(new LevelDTO(levelWithCondition));
        }

        /// Verifies that creating a level for an unknown user returns 404.
        @Test
        @DisplayName("should return 404 Not Found when user does not exist")
        void shouldReturnNotFound() {
            Mockito.when(levelService.createLevel(Mockito.any(),
                    Mockito.eq(ControllerSecurityTestConfig.DEFAULT_USER_ID)))
                    .thenThrow(new UserNotFoundException());

            restTestClient.post().uri("/levels")
                    .body(new CreateLevelDTO("T", "D",
                            new ClearCondition(new Condition.NoClearCondition(), 0)))
                    .exchange()
                    .expectStatus().isNotFound();
        }
    }

    /// Tests for POST /levels/clone.
    @Nested
    @DisplayName("POST /levels/clone")
    class CloneLevel {

        /// Verifies that cloning a level returns 200 OK and the cloned level DTO.
        @Test
        @DisplayName("should return 200 OK and the cloned level")
        void shouldReturnOk() {
            Mockito.when(userService.getById(ControllerSecurityTestConfig.DEFAULT_USER_ID))
                    .thenReturn(Optional.of(testUser));
            Mockito.when(levelService.cloneLevel(
                    Mockito.any(CloneLevelDTO.class),
                    Mockito.eq(testUser)))
                    .thenReturn(Optional.of(testLevel));

            restTestClient.post().uri("/levels/clone")
                    .body(new CloneLevelDTO(LEVEL_ID))
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(LevelDTO.class)
                    .isEqualTo(new LevelDTO(testLevel));
        }

        /// Verifies that cloning returns 403 Forbidden when cloning fails.
        @Test
        @DisplayName("should return 403 Forbidden when cloning fails")
        void shouldReturnForbidden() {
            Mockito.when(userService.getById(ControllerSecurityTestConfig.DEFAULT_USER_ID))
                    .thenReturn(Optional.of(testUser));
            Mockito.when(levelService.cloneLevel(Mockito.any(), Mockito.any()))
                    .thenReturn(Optional.empty());

            restTestClient.post().uri("/levels/clone")
                    .body(new CloneLevelDTO(LEVEL_ID))
                    .exchange()
                    .expectStatus().isForbidden();
        }

        /// Verifies that cloning returns 404 Not Found when user does not exist.
        @Test
        @DisplayName("should return 404 Not Found when user does not exist")
        void shouldReturnNotFound() {
            Mockito.when(userService.getById(ControllerSecurityTestConfig.DEFAULT_USER_ID))
                    .thenReturn(Optional.empty());

            restTestClient.post().uri("/levels/clone")
                    .body(new CloneLevelDTO(LEVEL_ID))
                    .exchange()
                    .expectStatus().isNotFound();
        }
    }

    /// Tests for PUT /levels/{id}/properties.
    @Nested
    @DisplayName("PUT /levels/{id}/properties")
    class UpdateLevelProperties {

        /// Verifies that updating level properties returns 200 OK.
        @Test
        @DisplayName("should return 200 OK and the updated level")
        void shouldReturnOk() {
            Mockito.when(userService.getById(ControllerSecurityTestConfig.DEFAULT_USER_ID))
                    .thenReturn(Optional.of(testUser));
            Mockito.when(levelService.updateLevelProperties(
                    Mockito.eq(testUser),
                    Mockito.eq(LEVEL_ID),
                    Mockito.any()))
                    .thenReturn(testLevel);

            final UpdateLevelDTO dto = new UpdateLevelDTO(
                    Optional.of("U"), Optional.empty(),
                    Optional.of(new ClearCondition(
                            new Condition.NoClearCondition(), 0)));
            restTestClient.put().uri(PROPERTIES_URI, LEVEL_ID)
                    .body(dto)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(LevelDTO.class)
                    .isEqualTo(new LevelDTO(testLevel));
        }

        /// Verifies that updating properties returns 404 when user is missing.
        @Test
        @DisplayName("should return 404 Not Found when user does not exist")
        void shouldReturnNotFoundForUser() {
            Mockito.when(userService.getById(ControllerSecurityTestConfig.DEFAULT_USER_ID))
                    .thenReturn(Optional.empty());

            final UpdateLevelDTO dto = new UpdateLevelDTO(
                    Optional.of("U"), Optional.empty(), Optional.empty());
            restTestClient.put().uri(PROPERTIES_URI, LEVEL_ID)
                    .body(dto)
                    .exchange()
                    .expectStatus().isNotFound();
        }

        /// Verifies that updating properties returns 404 when level is missing.
        @Test
        @DisplayName("should return 404 Not Found when level does not exist")
        void shouldReturnNotFoundForLevel() {
            Mockito.when(userService.getById(ControllerSecurityTestConfig.DEFAULT_USER_ID))
                    .thenReturn(Optional.of(testUser));
            Mockito.when(levelService.updateLevelProperties(
                    Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenThrow(new LevelNotFoundException());

            final UpdateLevelDTO dto = new UpdateLevelDTO(
                    Optional.of("U"), Optional.empty(), Optional.empty());
            restTestClient.put().uri(PROPERTIES_URI, LEVEL_ID)
                    .body(dto)
                    .exchange()
                    .expectStatus().isNotFound();
        }

        /// Verifies that updating properties returns 403 when user is unauthorized.
        @Test
        @DisplayName("should return 403 Forbidden when user is not the owner")
        void shouldReturnForbidden() {
            Mockito.when(userService.getById(ControllerSecurityTestConfig.DEFAULT_USER_ID))
                    .thenReturn(Optional.of(testUser));
            Mockito.when(levelService.updateLevelProperties(
                    Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenThrow(new ForbiddenUserException("Forbidden"));

            final UpdateLevelDTO dto = new UpdateLevelDTO(
                    Optional.of("U"), Optional.empty(), Optional.empty());
            restTestClient.put().uri(PROPERTIES_URI, LEVEL_ID)
                    .body(dto)
                    .exchange()
                    .expectStatus().isForbidden();
        }
    }

    /// Tests for DELETE /levels/{id}.
    @Nested
    @DisplayName("DELETE /levels/{id}")
    class DeleteLevel {

        @Test
        @DisplayName("should return 204 No Content")
        void shouldReturnNoContent() {
            Mockito.doNothing().when(levelService)
                    .deleteLevel(ControllerSecurityTestConfig.DEFAULT_USER_ID, LEVEL_ID);

            restTestClient.delete().uri("/levels/{id}", LEVEL_ID)
                    .exchange()
                    .expectStatus().isNoContent();
        }

        @Test
        @DisplayName("should return 404 Not Found when level does not exist")
        void shouldReturnNotFound() {
            Mockito.doThrow(new LevelNotFoundException())
                    .when(levelService).deleteLevel(
                            ControllerSecurityTestConfig.DEFAULT_USER_ID, LEVEL_ID);

            restTestClient.delete().uri("/levels/{id}", LEVEL_ID)
                    .exchange()
                    .expectStatus().isNotFound();
        }

        @Test
        @DisplayName("should return 403 Forbidden when user is not the owner")
        void shouldReturnForbidden() {
            Mockito.doThrow(new ForbiddenUserException("Forbidden"))
                    .when(levelService).deleteLevel(
                            ControllerSecurityTestConfig.DEFAULT_USER_ID, LEVEL_ID);

            restTestClient.delete().uri("/levels/{id}", LEVEL_ID)
                    .exchange()
                    .expectStatus().isForbidden();
        }
    }

    /// Tests for level attitudes.
    @Nested
    @DisplayName("Level attitudes")
    class LevelAttitude {

        @Test
        @DisplayName("PUT /levels/{id}/attitude should return 200 OK")
        void shouldUpdateLevelAttitude() {
            Mockito.doNothing().when(levelAttitudeService).setAttitude(
                    ControllerSecurityTestConfig.DEFAULT_USER_ID,
                    LEVEL_ID,
                    LevelAttitudeType.LIKE);

            restTestClient.put().uri("/levels/{id}/attitude", LEVEL_ID)
                    .body(new SetLevelAttitudeDTO(LevelAttitudeType.LIKE))
                    .exchange()
                    .expectStatus().isOk();
        }

        @Test
        @DisplayName("DELETE /levels/{id}/attitude should return 204 No Content")
        void shouldDeleteLevelAttitude() {
            Mockito.doNothing().when(levelAttitudeService)
                    .deleteAttitude(ControllerSecurityTestConfig.DEFAULT_USER_ID, LEVEL_ID);

            restTestClient.delete().uri("/levels/{id}/attitude", LEVEL_ID)
                    .exchange()
                    .expectStatus().isNoContent();
        }
    }
}
