package ch.usi.inf.bsc.sa4.lab02spring.controller;

import ch.usi.inf.bsc.sa4.lab02spring.configuration.ControllerSecurityTestConfig;
import ch.usi.inf.bsc.sa4.lab02spring.configuration.ControllerSecurityTestSupport;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.CreateUserDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.UserDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.UserProfileDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.service.AttemptService;
import ch.usi.inf.bsc.sa4.lab02spring.service.level.LevelService;
import ch.usi.inf.bsc.sa4.lab02spring.service.UserService;

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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/// Black-box tests for [UserController] endpoints. Verifies user retrieval,
/// profile aggregation, and identity resolution via security filters.
@WebMvcTest(controllers = UserController.class)
@AutoConfigureRestTestClient
@Import(ControllerSecurityTestConfig.class)
@SuppressWarnings("PMD.UnitTestShouldIncludeAssert")
@DisplayName("The User Controller")
class UserControllerTests {

    /// The fake authenticated user ID used across tests.
    private static final String USER_ID = "userid1";

    /// The fake authenticated user's display name.
    private static final String USER_NAME = "Alan Turing";

    /// Mocked service for user operations.
    @MockitoBean
    private UserService userService;

    /// Mocked service for level statistics.
    @MockitoBean
    private LevelService levelService;

    /// Mocked service for attempt statistics.
    @MockitoBean
    private AttemptService attemptService;

    /// Mocked decoder used by the resource-server security filter.
    @MockitoBean
    private JwtDecoder jwtDecoder;

    /// Client used to perform REST calls.
    @Autowired
    private RestTestClient restTestClient;

    /// Shared test user Alan Turing.
    private static User user1;

    /// Shared test user Grace Hopper.
    private static User user2;

    /// Initializes static test data.
    @BeforeAll
    static void setupData() {
        user1 = new User(USER_ID, USER_NAME);
        user2 = new User("userid2", "Grace Hopper");
    }

    /// Configures the mocked JWT decoder and common service stubs.
    @BeforeEach
    void setup() {
        ControllerSecurityTestSupport.mockJwtDecoder(this.jwtDecoder, USER_ID, USER_NAME);

        Mockito.when(userService.getById(ArgumentMatchers.any())).thenReturn(Optional.empty());
        Mockito.when(userService.getById(user1.getId())).thenReturn(Optional.of(user1));
        Mockito.when(userService.getById(user2.getId())).thenReturn(Optional.of(user2));
        Mockito.when(userService.getAllUsers()).thenReturn(List.of(user1, user2));
    }

    /// Tests for GET /users.
    @Nested
    @DisplayName("GET /users")
    class GetUsers {

        /// Verifies that all users are returned as DTOs.
        @Test
        @DisplayName("should return 200 OK and list of users")
        void returnsUserList() {
            final List<UserDTO> expectedUsers = List.of(new UserDTO(user1), new UserDTO(user2));

            ControllerSecurityTestSupport.withAuthAndCsrf(restTestClient.get().uri("/users"))
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(new ParameterizedTypeReference<List<UserDTO>>() {
                    })
                    .isEqualTo(expectedUsers);
        }
    }

    /// Tests for GET /users/{id}.
    @Nested
    @DisplayName("GET /users/{id}")
    class GetUser {

        /// Verifies that a single user is returned when ID exists.
        @Test
        @DisplayName("should return 200 OK and user when ID exists")
        void returnsUser() {
            final UserDTO expected = new UserDTO(user1);

            ControllerSecurityTestSupport.withAuthAndCsrf(restTestClient.get().uri("/users/{id}", USER_ID))
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(UserDTO.class)
                    .isEqualTo(expected);
        }

        /// Verifies that 404 is returned when user ID does not exist.
        @Test
        @DisplayName("should return 404 Not Found when ID does not exist")
        void returnsNotFound() {
            ControllerSecurityTestSupport.withAuthAndCsrf(restTestClient.get().uri("/users/{id}", "nonexistent"))
                    .exchange()
                    .expectStatus().isNotFound();
        }
    }

    /// Tests for GET /users/search.
    @Nested
    @DisplayName("GET /users/search")
    class SearchUsers {

        /// Verifies that searching users returns the matching list.
        @Test
        @DisplayName("should return 200 OK and matching users")
        void returnsMatchingUsers() {
            Mockito.when(userService.searchUsers("Alan")).thenReturn(List.of(user1));
            final List<UserDTO> expected = List.of(new UserDTO(user1));

            ControllerSecurityTestSupport.withAuthAndCsrf(restTestClient.get().uri("/users/search?query=Alan"))
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(new ParameterizedTypeReference<List<UserDTO>>() {})
                    .isEqualTo(expected);
        }
    }

    /// Tests for GET /users/profile.
    @Nested
    @DisplayName("GET /users/profile")
    class GetProfile {

        /// Verifies that the full profile is aggregated from multiple services for the
        /// authenticated user.
        @Test
        @DisplayName("should return 200 OK and full profile for authenticated user")
        void returnsAuthenticatedProfile() {
            Mockito.when(attemptService.getPlayedLevelsCount(user1)).thenReturn(5L);
            Mockito.when(attemptService.getCompletedLevelsCount(user1)).thenReturn(3L);
            Mockito.when(levelService.getCreatedLevelsByUser(user1)).thenReturn(Collections.emptyList());

            final UserProfileDTO expectedProfile = new UserProfileDTO(user1.getName(), 5L,
                    3L, Collections.emptyList());

            ControllerSecurityTestSupport.withAuthAndCsrf(restTestClient.get().uri("/users/profile"))
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(UserProfileDTO.class)
                    .isEqualTo(expectedProfile);
        }

        /// Verifies that 404 is returned if the authenticated user's profile is not found.
        @Test
        @DisplayName("should return 404 Not Found if profile user does not exist")
        void returnsNotFound() {
            // Change identity to non-existent user
            ControllerSecurityTestSupport.mockJwtDecoder(jwtDecoder, "missing", "Missing");

            ControllerSecurityTestSupport.withAuthAndCsrf(restTestClient.get().uri("/users/profile"))
                    .exchange()
                    .expectStatus().isNotFound();
        }
    }

    /// Tests for GET /users/me.
    @Nested
    @DisplayName("GET /users/me")
    class GetMe {

        /// Verifies that the endpoint returns the existing user when identity is
        /// resolved via filters.
        @Test
        @DisplayName("should return 200 OK and current user when already exists")
        void returnsExistingMe() {
            final UserDTO expectedUserDTO = new UserDTO(user1);

            ControllerSecurityTestSupport.withAuthAndCsrf(restTestClient.get().uri("/users/me"))
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(UserDTO.class)
                    .isEqualTo(expectedUserDTO);
        }

        /// Verifies that a new user is created and returned if identity is unknown but
        /// token is valid.
        @Test
        @DisplayName("should return 200 OK and create user when not exists")
        void createsAndReturnsNewMe() {
            final String newUserId = "new-userid";
            final String newUserName = "New User";

            // Re-mock decoder for this specific test case with different claims
            ControllerSecurityTestSupport.mockJwtDecoder(jwtDecoder, newUserId, newUserName);

            final User newUser = new User(newUserId, newUserName);
            Mockito.when(userService.getById(newUserId)).thenReturn(Optional.empty());
            Mockito.when(userService.createUser(ArgumentMatchers.any(CreateUserDTO.class))).thenReturn(newUser);

            final UserDTO expectedUserDTO = new UserDTO(newUser);

            ControllerSecurityTestSupport.withAuthAndCsrf(restTestClient.get().uri("/users/me"))
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(UserDTO.class)
                    .isEqualTo(expectedUserDTO);
        }
    }
}
