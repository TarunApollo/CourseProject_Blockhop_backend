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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/// Unit tests for [UserService].
@SpringBootTest
@DisplayName("The User Service")
@SuppressWarnings("PMD.TooManyStaticImports")
class UserServiceTests {

    /// Default user ID used in tests.
    private static final String USER_ID = "user-1";

    /// Default username used in tests.
    private static final String USER_NAME = "Mario";

    /// Default partial name used for search tests.
    private static final String PARTIAL_NAME = "Mar";

    /// Default DTO used for creation tests.
    private static final CreateUserDTO CREATE_DTO = new CreateUserDTO(USER_ID, USER_NAME);

    /// The service under test.
    @Autowired
    private UserService userService;

    /// Mocked repository to isolate tests.
    @MockitoBean
    private UserRepository userRepository;

    /// Shared test user entity.
    private User testUser;

    /// The expected User entity after creation mapping.
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

        /// Verifies that all users from the repository are returned.
        @Test
        @DisplayName("should return all users from the repository")
        void testGetAllUsersReturnsAll() {
            when(userRepository.findAll()).thenReturn(List.of(testUser));
            final List<User> result = userService.getAllUsers();
            Assertions.assertEquals(1, result.size());
            Assertions.assertSame(testUser, result.get(0));
            verify(userRepository).findAll();
        }
    }

    /// Tests for creating a user.
    @Nested
    @DisplayName("when creating a user")
    class CreateUser {

        /// Verifies mapping, saving, and returning of a new user.
        @Test
        @DisplayName("maps DTO to User, saves, and returns the result")
        void createdUserIsMappedAndReturned() {
            when(userRepository.save(refEq(expectedUser))).thenReturn(expectedUser);

            final User result = userService.createUser(CREATE_DTO);

            Assertions.assertSame(expectedUser, result);
        }
    }

    /// Tests for getting a user by ID.
    @Nested
    @DisplayName("when getting a user by ID")
    class GetById {

        /// Verifies that an existing user is found.
        @Test
        @DisplayName("should return a non-empty optional when the user exists")
        void testGetByIdFound() {
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
            final Optional<User> result = userService.getById(USER_ID);
            Assertions.assertTrue(result.isPresent());
            Assertions.assertSame(testUser, result.get());
        }

        /// Verifies that an empty optional is returned for unknown IDs.
        @Test
        @DisplayName("should return an empty optional when the user does not exist")
        void testGetByIdNotFound() {
            when(userRepository.findById("unknown")).thenReturn(Optional.empty());
            final Optional<User> result = userService.getById("unknown");
            Assertions.assertTrue(result.isEmpty());
        }
    }

    /// Tests for searching users.
    @Nested
    @DisplayName("when searching users by partial name")
    class SearchUsers {

        /// Verifies that matching users are returned.
        @Test
        @DisplayName("should return users whose name contains the search term")
        void testSearchUsersReturnsMatches() {
            when(userRepository.findByNameContaining(PARTIAL_NAME)).thenReturn(List.of(testUser));
            final List<User> result = userService.searchUsers(PARTIAL_NAME);
            Assertions.assertEquals(1, result.size());
            Assertions.assertSame(testUser, result.get(0));
            verify(userRepository).findByNameContaining(PARTIAL_NAME);
        }
    }
}
