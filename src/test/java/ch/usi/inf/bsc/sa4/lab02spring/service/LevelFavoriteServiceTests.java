package ch.usi.inf.bsc.sa4.lab02spring.service;

import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.LevelFavorite;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.repository.LevelFavoriteRepository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.List;

/// Unit tests for [LevelFavoriteService].
@ExtendWith(MockitoExtension.class)
@DisplayName("The LevelFavorite Service (Unit)")
@SuppressWarnings("NullAway")
class LevelFavoriteServiceTests {

    /// User identifier used across tests.
    private static final String USER_ID = "user-1";
    /// User display name used across tests.
    private static final String USER_NAME = "Mario";
    /// Level title used across tests.
    private static final String LEVEL_TITLE = "Test Level";
    /// Level description used across tests.
    private static final String LEVEL_DESCRIPTION = "desc";

    /// Mocked dependency.
    @Mock
    private LevelFavoriteRepository levelFavoriteRepository;

    /// Service under test, with mocks injected by Mockito.
    @InjectMocks
    private LevelFavoriteService levelFavoriteService;

    /// Test user, rebuilt per test.
    private User testUser;
    /// Test level, rebuilt per test.
    private Level testLevel;
    /// Expected favorite the service should save (timestamp ignored in
    /// comparisons because it is generated internally).
    private LevelFavorite expectedFavorite;

    /// Sets up reusable fixtures before each test.
    @BeforeEach
    void setup() {
        this.testUser = new User(USER_ID, USER_NAME);
        this.testLevel = new Level(LEVEL_TITLE, LEVEL_DESCRIPTION, this.testUser);
        this.expectedFavorite = new LevelFavorite(this.testUser, this.testLevel, ZonedDateTime.now());
    }

    @Nested
    @DisplayName("when adding a favorite")
    class AddFavorite {

        /// Verifies that a new favorite is persisted with the correct user
        /// and level when no favorite exists yet for this pair.
        @Test
        @DisplayName("saves a new favorite with the correct user and level")
        void savesNewFavorite() {
            Mockito.when(levelFavoriteRepository.existsByUserAndLevel(testUser, testLevel))
                    .thenReturn(false);

            levelFavoriteService.addFavorite(testUser, testLevel);

            Mockito.verify(levelFavoriteRepository)
                    .save(Mockito.refEq(expectedFavorite, "timestamp"));
        }

        /// Verifies that no save happens when the user has already favorited
        /// the level (idempotent PUT semantics).
        @Test
        @DisplayName("does not save when the favorite already exists")
        void doesNotSaveExistingFavorite() {
            Mockito.when(levelFavoriteRepository.existsByUserAndLevel(testUser, testLevel))
                    .thenReturn(true);

            levelFavoriteService.addFavorite(testUser, testLevel);

            Mockito.verify(levelFavoriteRepository, Mockito.never())
                    .save(Mockito.any(LevelFavorite.class));
        }
    }

    @Nested
    @DisplayName("when removing a favorite")
    class RemoveFavorite {

        /// Verifies the service delegates removal to the repository's
        /// derived delete query.
        @Test
        @DisplayName("delegates to repository.deleteByUserAndLevel")
        void delegatesDelete() {
            levelFavoriteService.removeFavorite(testUser, testLevel);

            Mockito.verify(levelFavoriteRepository).deleteByUserAndLevel(testUser, testLevel);
        }
    }

    @Nested
    @DisplayName("when retrieving favorites")
    class Retrieval {

        /// Verifies that retrieval returns exactly what the repository returns.
        @Test
        @DisplayName("returns the favorites reported by the repository")
        void returnsRepositoryResult() {
            Mockito.when(levelFavoriteRepository.findByUser(testUser))
                    .thenReturn(List.of(expectedFavorite));

            final List<LevelFavorite> result = levelFavoriteService.getFavoritesByUser(testUser);

            Assertions.assertEquals(1, result.size());
            Assertions.assertSame(expectedFavorite, result.get(0));
        }
    }
}