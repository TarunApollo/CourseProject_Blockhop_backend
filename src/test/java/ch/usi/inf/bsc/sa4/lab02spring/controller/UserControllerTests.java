package ch.usi.inf.bsc.sa4.lab02spring.controller;

import ch.usi.inf.bsc.sa4.lab02spring.configuration.ControllerSecurityTestConfig;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.CreateUserDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.UserDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.UserProfileDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.service.AttemptService;
import ch.usi.inf.bsc.sa4.lab02spring.service.UserService;
import ch.usi.inf.bsc.sa4.lab02spring.service.level.LevelService;

import org.junit.jupiter.api.*;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/// Black-box tests for [UserController] endpoints. Verifies user retrieval,
/// profile aggregation, and identity resolution via security filters.
@SpringBootTest
@AutoConfigureMockMvc
@Import(ControllerSecurityTestConfig.class)
@SuppressWarnings("PMD.UnitTestShouldIncludeAssert")
@DisplayName("The User Controller")
class UserControllerTests {

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
    static void setupData() {
        user1 = new User(ControllerSecurityTestConfig.DEFAULT_USER_ID,
                ControllerSecurityTestConfig.DEFAULT_USER_NAME);
        user2 = new User("userid2", "Grace Hopper");
    }

    @BeforeEach
    void setup() {
        Mockito.when(userService.getById(ArgumentMatchers.any())).thenReturn(Optional.empty());
        Mockito.when(userService.getById(user1.getId())).thenReturn(Optional.of(user1));
        Mockito.when(userService.getById(user2.getId())).thenReturn(Optional.of(user2));
        Mockito.when(userService.getAllUsers()).thenReturn(List.of(user1, user2));
    }

    @Nested
    @DisplayName("GET /users")
    class GetUsers {

        @Test
        @DisplayName("should return 200 OK and list of users")
        void returnsUserList() {
            final List<UserDTO> expectedUsers = List.of(new UserDTO(user1), new UserDTO(user2));

            restTestClient.get().uri("/users")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(new ParameterizedTypeReference<List<UserDTO>>() {
                    })
                    .isEqualTo(expectedUsers);
        }
    }

    @Nested
    @DisplayName("GET /users/{id}")
    class GetUser {

        @Test
        @DisplayName("should return 200 OK and user when ID exists")
        void returnsUser() {
            final UserDTO expected = new UserDTO(user1);

            restTestClient.get().uri("/users/{id}", ControllerSecurityTestConfig.DEFAULT_USER_ID)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(UserDTO.class)
                    .isEqualTo(expected);
        }

        @Test
        @DisplayName("should return 404 Not Found when ID does not exist")
        void returnsNotFound() {
            restTestClient.get().uri("/users/{id}", "nonexistent")
                    .exchange()
                    .expectStatus().isNotFound();
        }
    }

    @Nested
    @DisplayName("GET /users/search")
    class SearchUsers {

        @Test
        @DisplayName("should return 200 OK and matching users")
        void returnsMatchingUsers() {
            Mockito.when(userService.searchUsers("Alan")).thenReturn(List.of(user1));
            final List<UserDTO> expected = List.of(new UserDTO(user1));

            restTestClient.get().uri("/users/search?query=Alan")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(new ParameterizedTypeReference<List<UserDTO>>() {})
                    .isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("GET /users/profile")
    class GetProfile {

        @Test
        @DisplayName("should return 200 OK and full profile for authenticated user")
        void returnsAuthenticatedProfile() {
            Mockito.when(attemptService.getPlayedLevelsCount(user1)).thenReturn(5L);
            Mockito.when(attemptService.getCompletedLevelsCount(user1)).thenReturn(3L);
            Mockito.when(levelService.getCreatedLevelsByUser(user1)).thenReturn(Collections.emptyList());

            final UserProfileDTO expectedProfile = new UserProfileDTO(user1.getName(), 5L,
                    3L, Collections.emptyList());

            restTestClient.get().uri("/users/profile")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(UserProfileDTO.class)
                    .isEqualTo(expectedProfile);
        }

        @Disabled("AM: This test does not actually check what corresponds to its description.")
        @Test
        @DisplayName("should return 404 Not Found if profile user does not exist")
        void returnsNotFound() {
            restTestClient.get().uri("/users/profile")
//                    .header(ControllerSecurityTestConfig.USER_ID_HEADER, "missing")
//                    .header(ControllerSecurityTestConfig.USER_NAME_HEADER, "Missing")
                    .exchange()
                    .expectStatus().isNotFound();
        }
    }

    @Nested
    @DisplayName("GET /users/me")
    class GetMe {

        @Test
        @DisplayName("should return 200 OK and current user when already exists")
        void returnsExistingMe() {
            final UserDTO expectedUserDTO = new UserDTO(user1);

            restTestClient.get().uri("/users/me")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(UserDTO.class)
                    .isEqualTo(expectedUserDTO);
        }

        @Disabled("AM: There is actually no API to create new users, so this is not testing properly anything. Please remove.")
        @Test
        @DisplayName("should return 200 OK and create user when not exists")
        void createsAndReturnsNewMe() {
            final String newUserId = "new-userid";
            final String newUserName = "New User";

            final User newUser = new User(newUserId, newUserName);
            Mockito.when(userService.getById(newUserId)).thenReturn(Optional.empty());
            Mockito.when(userService.createUser(ArgumentMatchers.any(CreateUserDTO.class))).thenReturn(newUser);

            final UserDTO expectedUserDTO = new UserDTO(newUser);

            restTestClient.get().uri("/users/me")
//                    .header(ControllerSecurityTestConfig.USER_ID_HEADER, newUserId)
//                    .header(ControllerSecurityTestConfig.USER_NAME_HEADER, newUserName)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(UserDTO.class)
                    .isEqualTo(expectedUserDTO);
        }
    }
}
