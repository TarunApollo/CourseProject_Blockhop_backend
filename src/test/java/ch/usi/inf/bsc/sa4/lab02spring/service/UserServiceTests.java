package ch.usi.inf.bsc.sa4.lab02spring.service;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.CreateUserDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/// Unit tests for [UserService].
@SpringBootTest
@DisplayName("The User Service")
class UserServiceTests {

    /// Default user ID used in tests.
    private static final String USER_ID = "user-1";

    /// Default username used in tests.
    private static final String USER_NAME = "Mario";

    /// Default partial name used for search tests.
    private static final String PARTIAL_NAME = "Mar";

    /// Default [CreateUserDTO] used for creation tests.
    private static final CreateUserDTO CREATE_DTO = new CreateUserDTO(USER_ID, USER_NAME);

    /// The [UserService] under test.
    @Autowired
    private UserService userService;

    /// Mocked [UserRepository] to isolate tests.
    @MockitoBean
    private UserRepository userRepository;

    /// Shared test [User] entity.
    private User testUser;

    /// The expected [User] entity after creation mapping.
    private User expectedUser;

    /// Sets up test data before each test.
    @BeforeEach
    void setup() {
        this.testUser = new User(USER_ID, USER_NAME);
        this.expectedUser = new User(USER_ID, USER_NAME);
    }

    /// Tests for retrieving all users.
    @Nested
    @DisplayName("when getting all users")
    class GetAllUsers {

        /// Verifies that all users from the [UserRepository] are returned.
        @Test
        @DisplayName("should return all users from the repository")
        void testGetAllUsersReturnsAll() {
            Mockito.when(userRepository.findAll()).thenReturn(List.of(testUser));
            final List<User> result = userService.getAllUsers();
            Assertions.assertEquals(1, result.size());
            Assertions.assertSame(testUser, result.get(0));
            Mockito.verify(userRepository).findAll();
        }
    }

    /// Tests for creating a user.
    @Nested
    @DisplayName("when creating a user")
    class CreateUser {

        /// Verifies mapping, saving, and returning of a new [User].
        @Test
        @DisplayName("maps DTO to User, saves, and returns the result")
        void createdUserIsMappedAndReturned() {
            Mockito.when(userRepository.save(Mockito.refEq(expectedUser))).thenReturn(expectedUser);

            final User result = userService.createUser(CREATE_DTO);

            Assertions.assertSame(expectedUser, result);
        }
    }

    /// Tests for getting a user by ID.
    @Nested
    @DisplayName("when getting a user by ID")
    class GetById {

        /// Verifies that an existing [User] is found.
        @Test
        @DisplayName("should return a non-empty optional when the user exists")
        void testGetByIdFound() {
            Mockito.when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
            final Optional<User> result = userService.getById(USER_ID);
            Assertions.assertTrue(result.isPresent());
            Assertions.assertSame(testUser, result.get());
        }

        /// Verifies that an empty optional is returned for unknown IDs.
        @Test
        @DisplayName("should return an empty optional when the user does not exist")
        void testGetByIdNotFound() {
            Mockito.when(userRepository.findById("unknown")).thenReturn(Optional.empty());
            final Optional<User> result = userService.getById("unknown");
            Assertions.assertTrue(result.isEmpty());
        }
    }

    /// Tests for searching users.
    @Nested
    @DisplayName("when searching users by partial name")
    class SearchUsers {

        /// Verifies that matching [User]s are returned.
        @Test
        @DisplayName("should return users whose name contains the search term")
        void testSearchUsersReturnsMatches() {
            Mockito.when(userRepository.findByNameContaining(PARTIAL_NAME)).thenReturn(List.of(testUser));
            final List<User> result = userService.searchUsers(PARTIAL_NAME);
            Assertions.assertEquals(1, result.size());
            Assertions.assertSame(testUser, result.get(0));
            Mockito.verify(userRepository).findByNameContaining(PARTIAL_NAME);
        }
    }
}
