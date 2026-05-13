package ch.usi.inf.bsc.sa4.lab02spring.service.level;

import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.LevelFavorite;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.model.ClearCondition;
import ch.usi.inf.bsc.sa4.lab02spring.model.Condition;
import ch.usi.inf.bsc.sa4.lab02spring.repository.LevelFavoriteRepository;
import ch.usi.inf.bsc.sa4.lab02spring.utils.ForbiddenLevelActionException;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

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

/// Unit tests for [LevelFavoriteService].
@ExtendWith(MockitoExtension.class)
@DisplayName("The LevelFavorite Service (Unit)")
@SuppressWarnings("NullAway")
class LevelFavoriteServiceTests {

    /// Default user ID used in tests.
    private static final String USER_ID = "user-1";

    /// Default username used in tests.
    private static final String USER_NAME = "Mario";

    /// Default level title used in tests.
    private static final String LEVEL_TITLE = "Test Level";

    /// Default level description used in tests.
    private static final String LEVEL_DESCRIPTION = "desc";

    /// Default level id used in tests.
    private static final String LEVEL_ID = "level-1";

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
        this.testLevel = createLevel(true);
        this.expectedFavorite = new LevelFavorite(this.testUser, this.testLevel, ZonedDateTime.now());
    }

    private Level createLevel(final boolean published) {
        return new Level(this.testUser, LEVEL_TITLE, LEVEL_DESCRIPTION, published,
                new ClearCondition(new Condition.NoClearCondition(), 0), Map.of(), Map.of());
    }

    /// Tests for the addFavorite method.
    @Nested
    @DisplayName("when adding a favorite")
    class AddFavorite {

        /// Verifies that a new favorite is persisted with the correct user
        /// and level when no favorite exists yet for this pair.
        @Test
        @DisplayName("saves a new favorite with the correct user and level")
        void savesNewFavorite() {
            Mockito.when(levelFavoriteRepository.existsByUserAndLevel(testUser, testLevel))
                    .thenReturn(Boolean.FALSE);

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
                    .thenReturn(Boolean.TRUE);

            levelFavoriteService.addFavorite(testUser, testLevel);

            Mockito.verify(levelFavoriteRepository, Mockito.never())
                    .save(Mockito.any(LevelFavorite.class));
        }

        /// Verifies that unpublished levels cannot be favorited.
        @Test
        @DisplayName("rejects unpublished levels")
        void rejectsUnpublishedLevel() {
            final Level unpublishedLevel = createLevel(false);

            Assertions.assertThrows(ForbiddenLevelActionException.class,
                    () -> levelFavoriteService.addFavorite(testUser, unpublishedLevel));

            Mockito.verify(levelFavoriteRepository, Mockito.never())
                    .save(Mockito.any(LevelFavorite.class));
        }
    }

    /// Tests for the removeFavorite method.
    @Nested
    @DisplayName("when removing a favorite")
    class RemoveFavorite {

        /// Verifies the service delegates removal to the repository's
        /// derived delete query using only the level id.
        @Test
        @DisplayName("delegates to repository.deleteByUserAndLevelId")
        void delegatesDelete() {
            levelFavoriteService.removeFavorite(testUser, LEVEL_ID);

            Mockito.verify(levelFavoriteRepository).deleteByUserAndLevelId(testUser, LEVEL_ID);
        }
    }

    /// Tests for the getFavoritesByUser method.
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
