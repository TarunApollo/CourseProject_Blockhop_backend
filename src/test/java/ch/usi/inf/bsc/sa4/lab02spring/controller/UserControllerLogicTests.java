package ch.usi.inf.bsc.sa4.lab02spring.controller;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.CreateUserDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.UserDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.UserProfileDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.LevelFavorite;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.service.AttemptService;
import ch.usi.inf.bsc.sa4.lab02spring.service.level.LevelService;
import ch.usi.inf.bsc.sa4.lab02spring.service.UserService;
import ch.usi.inf.bsc.sa4.lab02spring.service.LevelFavoriteService;
import ch.usi.inf.bsc.sa4.lab02spring.utils.AuthUtils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * UserControllerLogicTests (black-box testing).
 * Goal: Test only the controller's input mapping, dependency interactions, and
 * output serialization.
 * We disable Spring's Security and mock the existence of authenticated users,
 * testing business-logic only.
 */
@WebMvcTest(controllers = UserController.class, excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        OAuth2ClientAutoConfiguration.class,
        OAuth2ResourceServerAutoConfiguration.class
})
@AutoConfigureRestTestClient
@DisplayName("User Controller Logic Tests")
class UserControllerLogicTests {

    /**
     * The mocked user service.
     */
    @MockitoBean
    private UserService userService;

    /**
     * The mocked level service.
     */
    @MockitoBean
    private LevelService levelService;

    /**
     * The mocked attempt service.
     */
    @MockitoBean
    private AttemptService attemptService;

    /**
     * The mocked favorite service.
     */
    @MockitoBean
    private LevelFavoriteService levelFavoriteService;

    /**
     * The RestTestClient for performing requests.
     */
    @Autowired
    private RestTestClient restTestClient;

    /**
     * The first test user.
     */
    private static User user1;

    /**
     * The second test user.
     */
    private static User user2;

    /**
     * Initializes static test data.
     */
    @BeforeAll
    static void setupData() {
        user1 = new User("userid1", "Alan Turing");
        user2 = new User("userid2", "Grace Hopper");
    }

    /**
     * Sets up the test environment.
     */
    @BeforeEach
    /* default */ void setup() {
        Mockito.when(userService.getById(Mockito.any())).thenReturn(Optional.empty());
        Mockito.when(userService.getById(user1.getId())).thenReturn(Optional.of(user1));
        Mockito.when(userService.getById(user2.getId())).thenReturn(Optional.of(user2));
        Mockito.when(userService.getAllUsers()).thenReturn(List.of(user1, user2));
    }

    /**
     * Verifies that GET /users returns a list of all users.
     */
    @Test
    @DisplayName("GET /users should return list of users")
    void testGetUsers() {
        final List<UserDTO> expectedUsers = List.of(new UserDTO(user1), new UserDTO(user2));
        Assertions.assertDoesNotThrow(() -> restTestClient.get().uri("/users")
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<List<UserDTO>>() {
                })
                .isEqualTo(expectedUsers));
    }

    /**
     * Verifies that GET /users/profile
     * returns the full profile for an authenticated user.
     */
    @Test
    @DisplayName("GET /users/profile should return full profile for authenticated user")
    void testGetProfile() {
        try (MockedStatic<AuthUtils> mockedAuth = Mockito.mockStatic(AuthUtils.class)) {
            mockedAuth.when(() -> AuthUtils.getUserIdFromAuth(Mockito.any())).thenReturn("userid1");

            Mockito.when(attemptService.getPlayedLevelsCount(user1)).thenReturn(5L);
            Mockito.when(attemptService.getCompletedLevelsCount(user1)).thenReturn(3L);
            Mockito.when(levelService.getCreatedLevelsByUser(user1)).thenReturn(Collections.emptyList());

            final UserProfileDTO expectedProfile = new UserProfileDTO(user1.getName(), 5L,
                    3L, Collections.emptyList());

            Assertions.assertDoesNotThrow(() -> restTestClient.get().uri("/users/profile")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(UserProfileDTO.class)
                    .isEqualTo(expectedProfile));
        }
    }

    /**
     * Verifies that GET /users/me returns the current user when it already exists.
     */
    @Test
    @DisplayName("GET /users/me should return current user when already exists")
    void testGetMeExistingUser() {
        try (MockedStatic<AuthUtils> mockedAuth = Mockito.mockStatic(AuthUtils.class)) {
            mockedAuth.when(() -> AuthUtils.getUserIdFromAuth(Mockito.any())).thenReturn("userid1");
            mockedAuth.when(() -> AuthUtils.getUserNameFromAuth(Mockito.any())).thenReturn("Alan Turing");

            final UserDTO expectedUserDTO = new UserDTO(user1);
            Assertions.assertDoesNotThrow(() -> restTestClient.get().uri("/users/me")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(UserDTO.class)
                    .isEqualTo(expectedUserDTO));
        }
    }

    /**
     * Verifies that GET /users/me creates and returns a user when it does not
     * exist.
     */
    @Test
    @DisplayName("GET /users/me should create and return user when not exists")
    void testGetMeNewUser() {
        try (MockedStatic<AuthUtils> mockedAuth = Mockito.mockStatic(AuthUtils.class)) {
            mockedAuth.when(() -> AuthUtils.getUserIdFromAuth(Mockito.any())).thenReturn("new-userid");
            mockedAuth.when(() -> AuthUtils.getUserNameFromAuth(Mockito.any())).thenReturn("New User");

            final User newUser = new User("new-userid", "New User");
            Mockito.when(userService.getById("new-userid")).thenReturn(Optional.empty());
            Mockito.when(userService.createUser(Mockito.any(CreateUserDTO.class))).thenReturn(newUser);

            final UserDTO expectedUserDTO = new UserDTO(newUser);
            Assertions.assertDoesNotThrow(() -> restTestClient.get().uri("/users/me")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(UserDTO.class)
                    .isEqualTo(expectedUserDTO));
        }
    }

    /**
     * Verifies that GET /users/me/favorites returns the list of
     * favorited levels for the authenticated user.
     */
    @Test
    @DisplayName("GET /users/me/favorites should return the favorited levels")
    void testGetFavorites() {
        try (MockedStatic<AuthUtils> mockedAuth = Mockito.mockStatic(AuthUtils.class)) {
            mockedAuth.when(() -> AuthUtils.getUserIdFromAuth(Mockito.any())).thenReturn("userid1");

            final Level favoritedLevel = new Level("Favorited", "desc", user1);
            final LevelFavorite favorite = new LevelFavorite(user1, favoritedLevel, ZonedDateTime.now());
            Mockito.when(levelFavoriteService.getFavoritesByUser(user1)).thenReturn(List.of(favorite));

            Assertions.assertDoesNotThrow(() -> restTestClient.get().uri("/users/me/favorites")
                    .exchange()
                    .expectStatus().isOk());

            Mockito.verify(levelFavoriteService).getFavoritesByUser(user1);
        }
    }

}
