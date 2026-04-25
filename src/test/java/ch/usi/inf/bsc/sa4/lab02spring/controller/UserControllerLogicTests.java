package ch.usi.inf.bsc.sa4.lab02spring.controller;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.CreateUserDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.UserDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.UserProfileDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.service.AttemptService;
import ch.usi.inf.bsc.sa4.lab02spring.service.LevelService;
import ch.usi.inf.bsc.sa4.lab02spring.service.UserService;
import ch.usi.inf.bsc.sa4.lab02spring.utils.AuthUtils;

import org.junit.jupiter.api.*;
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * UserControllerLogicTests (black-box testing)
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
public class UserControllerLogicTests {

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private LevelService levelService;

    @MockitoBean
    private AttemptService attemptService;

    @Autowired
    private RestTestClient restTestClient;

    private static User user1;
    private static User user2;

    @BeforeAll
    public static void testDataSetup() {
        user1 = new User("userid1", "Alan Turing");
        user2 = new User("userid2", "Grace Hopper");
    }

    @BeforeEach
    void setup() {
        when(userService.getById(any())).thenReturn(Optional.empty());
        when(userService.getById(user1.getId())).thenReturn(Optional.of(user1));
        when(userService.getById(user2.getId())).thenReturn(Optional.of(user2));
        when(userService.getAllUsers()).thenReturn(List.of(user1, user2));
    }

    @Test
    @DisplayName("GET /users should return list of users")
    public void testGetUsers() {
        var expectedUsers = List.of(new UserDTO(user1), new UserDTO(user2));
        restTestClient.get().uri("/users")
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<List<UserDTO>>() {
                })
                .isEqualTo(expectedUsers);
    }

    @Test
    @DisplayName("GET /users/profile should return full profile for authenticated user")
    public void testGetProfile() {
        try (MockedStatic<AuthUtils> mockedAuth = Mockito.mockStatic(AuthUtils.class)) {
            mockedAuth.when(() -> AuthUtils.getUserIdFromAuth(any())).thenReturn("userid1");

            when(attemptService.getPlayedLevelsCount(user1)).thenReturn(5L);
            when(attemptService.getCompletedLevelsCount(user1)).thenReturn(3L);
            when(levelService.getCreatedLevelsByUser(user1)).thenReturn(Collections.emptyList());

            var expectedProfile = new UserProfileDTO(user1.getName(), 5L,
                    3L, Collections.emptyList());

            restTestClient.get().uri("/users/profile")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(UserProfileDTO.class)
                    .isEqualTo(expectedProfile);
        }
    }

    @Test
    @DisplayName("GET /users/me should return current user when already exists")
    public void testGetMeExistingUser() {
        try (MockedStatic<AuthUtils> mockedAuth = Mockito.mockStatic(AuthUtils.class)) {
            mockedAuth.when(() -> AuthUtils.getUserIdFromAuth(any())).thenReturn("userid1");
            mockedAuth.when(() -> AuthUtils.getUserNameFromAuth(any())).thenReturn("Alan Turing");

            var expectedUserDTO = new UserDTO(user1);
            restTestClient.get().uri("/users/me")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(UserDTO.class)
                    .isEqualTo(expectedUserDTO);
        }
    }

    @Test
    @DisplayName("GET /users/me should create and return user when not exists")
    public void testGetMeNewUser() {
        try (MockedStatic<AuthUtils> mockedAuth = Mockito.mockStatic(AuthUtils.class)) {
            mockedAuth.when(() -> AuthUtils.getUserIdFromAuth(any())).thenReturn("new-userid");
            mockedAuth.when(() -> AuthUtils.getUserNameFromAuth(any())).thenReturn("New User");

            User newUser = new User("new-userid", "New User");
            when(userService.getById("new-userid")).thenReturn(Optional.empty());
            when(userService.createUser(any(CreateUserDTO.class))).thenReturn(newUser);

            var expectedUserDTO = new UserDTO(newUser);
            restTestClient.get().uri("/users/me")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(UserDTO.class)
                    .isEqualTo(expectedUserDTO);
        }
    }

}
