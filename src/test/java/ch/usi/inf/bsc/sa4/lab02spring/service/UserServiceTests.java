package ch.usi.inf.bsc.sa4.lab02spring.service;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.CreateUserDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the UserService.
 * Goal: Test service logic in isolation using Mockito.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("The User Service (Unit)")
@SuppressWarnings({ "PMD.AtLeastOneConstructor", "NullAway" })
/* default */ class UserServiceTests {

    /** The mocked user repository. */
    @Mock
    private UserRepository userRepository;

    /** The service under test. */
    @InjectMocks
    private UserService userService;

    /** Default user ID used in tests. */
    private static final String USER_ID = "user-1";

    /** Default username used in tests. */
    private static final String USER_NAME = "Mario";

    /** Default partial name used for search tests. */
    private static final String PARTIAL_NAME = "Mar";

    /** The test user. */
    private User testUser;

    /**
     * Sets up test data before each test.
     */
    @BeforeEach
    /* default */ void setup() {
        this.testUser = new User(USER_ID, USER_NAME);
    }

    /**
     * Tests for the getAllUsers method.
     */
    @Nested
    @DisplayName("when getting all users")
    @SuppressWarnings("PMD.AtLeastOneConstructor")
    /* default */ class GetAllUsers {

        /**
         * Verifies that all users from the repository are returned.
         */
        @Test
        @DisplayName("should return all users from the repository")
        /* default */ void testGetAllUsersReturnsAll() {
            when(userRepository.findAll()).thenReturn(List.of(testUser));
            final List<User> result = userService.getAllUsers();
            assertEquals(1, result.size());
        }

        /**
         * Verifies that the repository is queried.
         */
        @Test
        @DisplayName("should delegate to the repository")
        /* default */ void testGetAllUsersCallsRepo() {
            when(userRepository.findAll()).thenReturn(List.of(testUser));
            userService.getAllUsers();
            verify(userRepository).findAll();
        }
    }

    /**
     * Tests for the createUser method.
     */
    @Nested
    @DisplayName("when creating a user")
    @SuppressWarnings("PMD.AtLeastOneConstructor")
    /* default */ class CreateUser {

        /**
         * Verifies that the saved user is returned.
         */
        @Test
        @DisplayName("should return the saved user")
        /* default */ void testCreateUserReturnsUser() {
            final CreateUserDTO dto = new CreateUserDTO(USER_ID, USER_NAME);
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            final User result = userService.createUser(dto);
            assertNotNull(result);
        }

        /**
         * Verifies that the returned user has the correct name.
         */
        @Test
        @DisplayName("should return user with the name from the DTO")
        /* default */ void testCreateUserHasCorrectName() {
            final CreateUserDTO dto = new CreateUserDTO(USER_ID, USER_NAME);
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            final User result = userService.createUser(dto);
            assertEquals(USER_NAME, result.getName());
        }

        /**
         * Verifies that the repository save method is called.
         */
        @Test
        @DisplayName("should persist the user via the repository")
        /* default */ void testCreateUserPersists() {
            final CreateUserDTO dto = new CreateUserDTO(USER_ID, USER_NAME);
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            userService.createUser(dto);
            verify(userRepository).save(any(User.class));
        }
    }

    /**
     * Tests for the getById method.
     */
    @Nested
    @DisplayName("when getting a user by ID")
    @SuppressWarnings("PMD.AtLeastOneConstructor")
    /* default */ class GetById {

        /**
         * Verifies that an existing user is found.
         */
        @Test
        @DisplayName("should return a non-empty optional when the user exists")
        /* default */ void testGetByIdFound() {
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
            final Optional<User> result = userService.getById(USER_ID);
            assertTrue(result.isPresent());
        }

        /**
         * Verifies that an empty optional is returned for unknown IDs.
         */
        @Test
        @DisplayName("should return an empty optional when the user does not exist")
        /* default */ void testGetByIdNotFound() {
            when(userRepository.findById("unknown")).thenReturn(Optional.empty());
            final Optional<User> result = userService.getById("unknown");
            assertTrue(result.isEmpty());
        }
    }

    /**
     * Tests for the searchUsers method.
     */
    @Nested
    @DisplayName("when searching users by partial name")
    @SuppressWarnings("PMD.AtLeastOneConstructor")
    /* default */ class SearchUsers {

        /**
         * Verifies that matching users are returned.
         */
        @Test
        @DisplayName("should return users whose name contains the search term")
        /* default */ void testSearchUsersReturnsMatches() {
            when(userRepository.findByNameContaining(PARTIAL_NAME)).thenReturn(List.of(testUser));
            final List<User> result = userService.searchUsers(PARTIAL_NAME);
            assertEquals(1, result.size());
        }

        /**
         * Verifies that the repository is queried with the correct term.
         */
        @Test
        @DisplayName("should delegate to the repository with the given partial name")
        /* default */ void testSearchUsersCallsRepo() {
            when(userRepository.findByNameContaining(PARTIAL_NAME)).thenReturn(List.of());
            userService.searchUsers(PARTIAL_NAME);
            verify(userRepository).findByNameContaining(PARTIAL_NAME);
        }
    }
}
