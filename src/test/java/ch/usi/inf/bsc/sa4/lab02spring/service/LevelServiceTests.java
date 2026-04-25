package ch.usi.inf.bsc.sa4.lab02spring.service;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.AttemptDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.CloneLevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.CreateLevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.UpdateLevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.converter.LayerToTiledMapConverter;
import ch.usi.inf.bsc.sa4.lab02spring.model.GroundObject;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.Position;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.repository.AttemptRepository;
import ch.usi.inf.bsc.sa4.lab02spring.repository.LevelRepository;
import ch.usi.inf.bsc.sa4.lab02spring.repository.LevelThumbnailRepository;
import ch.usi.inf.bsc.sa4.lab02spring.repository.ThumbnailRepository;
import ch.usi.inf.bsc.sa4.lab02spring.repository.UserRepository;
import ch.usi.inf.bsc.sa4.lab02spring.utils.DateRangePreset;
import ch.usi.inf.bsc.sa4.lab02spring.utils.LevelNotFoundException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.PublishedLevelSortBy;
import ch.usi.inf.bsc.sa4.lab02spring.utils.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the LevelService.
 * Goal: Test service logic in isolation using Mockito.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("The Level Service (Unit)")
@SuppressWarnings({ "PMD.AtLeastOneConstructor", "NullAway" })
/* default */ class LevelServiceTests {

    /** The mocked level repository. */
    @Mock
    private LevelRepository levelRepository;

    /** The mocked user service. */
    @Mock
    private UserService userService;

    /** The mocked user repository. */
    @Mock
    private UserRepository userRepository;

    /** The mocked attempt repository. */
    @Mock
    private AttemptRepository attemptRepository;

    /** The mocked level thumbnail repository. */
    @Mock
    private LevelThumbnailRepository levelThumbnailRepository;

    /** The mocked thumbnail repository. */
    @Mock
    private ThumbnailRepository thumbnailRepository;

    /** The mocked attempt service. */
    @Mock
    private AttemptService attemptService;

    /** The mocked tileset service. */
    @Mock
    private TileSetService tileSetService;

    /** The mocked layer converter. */
    @Mock
    private LayerToTiledMapConverter layerToTiledMapConverter;

    /** The service under test. */
    @InjectMocks
    private LevelService levelService;

    /** Default user ID used in tests. */
    private static final String USER_ID = "user-1";

    /** Default level ID used in tests. */
    private static final String LEVEL_ID = "level-1";

    /** Default level title used in tests. */
    private static final String LEVEL_TITLE = "Test Level";

    /** Default level description used in tests. */
    private static final String LEVEL_DESC = "Test Description";

    /** Default username used in tests. */
    private static final String USER_NAME = "Mario";

    /** Expected success message from submitAttempt. */
    private static final String SUCCESS_MSG = "Successful level submission.";

    /** The test user. */
    private User testUser;

    /** The test level. */
    private Level testLevel;

    /**
     * Sets up test data before each test.
     */
    @BeforeEach
    /* default */ void setup() {
        this.testUser = new User(USER_ID, USER_NAME);
        this.testLevel = new Level(LEVEL_TITLE, LEVEL_DESC, this.testUser);
    }

    /**
     * Tests for the createLevel method.
     */
    @Nested
    @DisplayName("when creating a level")
    @SuppressWarnings("PMD.AtLeastOneConstructor")
    /* default */ class CreateLevel {

        /**
         * Verifies that a new level is saved and returned.
         */
        @Test
        @DisplayName("should save and return a new level for an existing user")
        /* default */ void testCreateLevelReturnsLevel() {
            final CreateLevelDTO dto = new CreateLevelDTO(LEVEL_TITLE, LEVEL_DESC);
            when(userService.getById(USER_ID)).thenReturn(Optional.of(testUser));
            when(levelRepository.save(any(Level.class))).thenReturn(testLevel);
            final Level result = levelService.createLevel(dto, USER_ID);
            assertNotNull(result);
        }

        /**
         * Verifies that UserNotFoundException is thrown when user is not found.
         */
        @Test
        @DisplayName("should throw UserNotFoundException when user does not exist")
        /* default */ void testCreateLevelUserNotFound() {
            final CreateLevelDTO dto = new CreateLevelDTO(LEVEL_TITLE, LEVEL_DESC);
            when(userService.getById(USER_ID)).thenReturn(Optional.empty());
            assertThrows(UserNotFoundException.class, () -> levelService.createLevel(dto, USER_ID));
        }
    }

    /**
     * Tests for the cloneLevel method.
     */
    @Nested
    @DisplayName("when cloning a level")
    @SuppressWarnings("PMD.AtLeastOneConstructor")
    /* default */ class CloneLevel {

        /**
         * Verifies that a level owned by the user can be cloned.
         */
        @Test
        @DisplayName("should return a non-empty optional when the level is owned by the user")
        /* default */ void testCloneLevelOwned() {
            final CloneLevelDTO dto = new CloneLevelDTO(LEVEL_ID);
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(testLevel));
            when(levelRepository.findByCreator(testUser)).thenReturn(List.of());
            when(levelRepository.save(any(Level.class))).thenReturn(testLevel);
            final Optional<Level> result = levelService.cloneLevel(dto, testUser);
            assertTrue(result.isPresent());
        }

        /**
         * Verifies that a level not owned by the user returns empty.
         */
        @Test
        @DisplayName("should return empty optional when the level is not owned by the user")
        /* default */ void testCloneLevelNotOwned() {
            final User otherUser = new User("user-2", "Luigi");
            final CloneLevelDTO dto = new CloneLevelDTO(LEVEL_ID);
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(testLevel));
            final Optional<Level> result = levelService.cloneLevel(dto, otherUser);
            assertTrue(result.isEmpty());
        }
    }

    /**
     * Tests for the deleteLevel method.
     */
    @Nested
    @DisplayName("when deleting a level")
    @SuppressWarnings("PMD.AtLeastOneConstructor")
    /* default */ class DeleteLevel {

        /**
         * Verifies that an owned unpublished level is deleted.
         */
        @Test
        @DisplayName("should delete an owned unpublished level")
        /* default */ void testDeleteLevel() {
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(testLevel));
            levelService.deleteLevel(USER_ID, LEVEL_ID);
            verify(levelRepository).deleteById(LEVEL_ID);
        }

        /**
         * Verifies that LevelNotFoundException is thrown when level does not exist.
         */
        @Test
        @DisplayName("should throw LevelNotFoundException when level does not exist")
        /* default */ void testDeleteLevelNotFound() {
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.empty());
            assertThrows(LevelNotFoundException.class, () -> levelService.deleteLevel(USER_ID, LEVEL_ID));
        }
    }

    /**
     * Tests for the updateLevelProperties method.
     */
    @Nested
    @DisplayName("when updating level properties")
    @SuppressWarnings("PMD.AtLeastOneConstructor")
    /* default */ class UpdateLevelProperties {

        /**
         * Verifies that the updated level is saved and returned.
         */
        @Test
        @DisplayName("should save and return the updated level")
        /* default */ void testUpdateLevelReturnsLevel() {
            final UpdateLevelDTO dto = new UpdateLevelDTO(Optional.empty(), Optional.empty(), Optional.empty());
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(testLevel));
            when(levelRepository.save(testLevel)).thenReturn(testLevel);
            final Level result = levelService.updateLevelProperties(testUser, LEVEL_ID, dto);
            assertNotNull(result);
        }

        /**
         * Verifies that LevelNotFoundException is thrown when level does not exist.
         */
        @Test
        @DisplayName("should throw LevelNotFoundException when level does not exist")
        /* default */ void testUpdateLevelNotFound() {
            final UpdateLevelDTO dto = new UpdateLevelDTO(Optional.empty(), Optional.empty(), Optional.empty());
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.empty());
            assertThrows(LevelNotFoundException.class,
                    () -> levelService.updateLevelProperties(testUser, LEVEL_ID, dto));
        }
    }

    /**
     * Tests for the publish method.
     */
    @Nested
    @DisplayName("when publishing a level")
    @SuppressWarnings("PMD.AtLeastOneConstructor")
    /* default */ class Publish {

        /**
         * Verifies that a level is published and returned.
         */
        @Test
        @DisplayName("should publish and return the level")
        /* default */ void testPublishReturnsLevel() {
            final Level mockLevel = Mockito.mock(Level.class);
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(mockLevel));
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
            when(levelRepository.save(mockLevel)).thenReturn(mockLevel);
            final Level result = levelService.publish(USER_ID, LEVEL_ID);
            assertEquals(mockLevel, result);
        }

        /**
         * Verifies that publish is called on the level domain object.
         */
        @Test
        @DisplayName("should call publish on the domain level")
        /* default */ void testPublishCallsDomainMethod() {
            final Level mockLevel = Mockito.mock(Level.class);
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(mockLevel));
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
            when(levelRepository.save(mockLevel)).thenReturn(mockLevel);
            levelService.publish(USER_ID, LEVEL_ID);
            verify(mockLevel).publish(USER_ID);
        }

        /**
         * Verifies that LevelNotFoundException is thrown when level does not exist.
         */
        @Test
        @DisplayName("should throw LevelNotFoundException when level does not exist")
        /* default */ void testPublishLevelNotFound() {
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.empty());
            assertThrows(LevelNotFoundException.class, () -> levelService.publish(USER_ID, LEVEL_ID));
        }

        /**
         * Verifies that UserNotFoundException is thrown when user does not exist.
         */
        @Test
        @DisplayName("should throw UserNotFoundException when user does not exist")
        /* default */ void testPublishUserNotFound() {
            final Level mockLevel = Mockito.mock(Level.class);
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(mockLevel));
            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());
            assertThrows(UserNotFoundException.class, () -> levelService.publish(USER_ID, LEVEL_ID));
        }
    }

    /**
     * Tests for the unpublishLevel method.
     */
    @Nested
    @DisplayName("when unpublishing a level")
    @SuppressWarnings("PMD.AtLeastOneConstructor")
    /* default */ class UnpublishLevel {

        /**
         * Verifies that the unpublished level is returned.
         */
        @Test
        @DisplayName("should return the unpublished level")
        /* default */ void testUnpublishReturnsLevel() {
            final Level mockLevel = Mockito.mock(Level.class);
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(mockLevel));
            when(levelRepository.save(mockLevel)).thenReturn(mockLevel);
            final Level result = levelService.unpublishLevel(USER_ID, LEVEL_ID);
            assertNotNull(result);
        }
    }

    /**
     * Tests for the getPublishedLevels method.
     */
    @Nested
    @DisplayName("when getting published levels")
    @SuppressWarnings("PMD.AtLeastOneConstructor")
    /* default */ class GetPublishedLevels {

        /**
         * Verifies that published levels are returned sorted by clear rate.
         */
        @Test
        @DisplayName("should return published levels sorted by clear rate")
        /* default */ void testGetPublishedLevelsClearRate() {
            when(levelRepository.findByPublishedTrue()).thenReturn(List.of(testLevel));
            when(attemptRepository.countByLevel(testLevel)).thenReturn(10L);
            when(attemptRepository.countByLevelAndCompletedTrue(testLevel)).thenReturn(5L);
            final var result = levelService.getPublishedLevels(
                    PublishedLevelSortBy.CLEAR_RATE, DateRangePreset.AllTimeDateRangePreset.ALL_TIME);
            assertEquals(1, result.size());
        }
    }

    /**
     * Tests for the getById method.
     */
    @Nested
    @DisplayName("when getting a level by ID")
    @SuppressWarnings("PMD.AtLeastOneConstructor")
    /* default */ class GetById {

        /**
         * Verifies that an existing level is found.
         */
        @Test
        @DisplayName("should return a non-empty optional when the level exists")
        /* default */ void testGetByIdFound() {
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(testLevel));
            final Optional<Level> result = levelService.getById(LEVEL_ID);
            assertTrue(result.isPresent());
        }

        /**
         * Verifies that an empty optional is returned for unknown IDs.
         */
        @Test
        @DisplayName("should return an empty optional when the level does not exist")
        /* default */ void testGetByIdNotFound() {
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.empty());
            final Optional<Level> result = levelService.getById(LEVEL_ID);
            assertTrue(result.isEmpty());
        }
    }

    /**
     * Tests for the validateLevelSubmission method.
     */
    @Nested
    @DisplayName("when validating a level submission")
    @SuppressWarnings("PMD.AtLeastOneConstructor")
    /* default */ class ValidateLevelSubmission {

        /**
         * Verifies that matching world layers return true.
         */
        @Test
        @DisplayName("should return true when world layers match")
        /* default */ void testValidateSubmissionMatch() {
            final AttemptDTO dto = new AttemptDTO(
                    Map.of(), new Position(0, 0), ZonedDateTime.now(), Duration.ZERO, false);
            final boolean result = levelService.validateLevelSubmission(testLevel, dto);
            assertTrue(result);
        }

        /**
         * Verifies that non-matching world layers return false.
         */
        @Test
        @DisplayName("should return false when world layers do not match")
        /* default */ void testValidateSubmissionNoMatch() {
            final Map<Position, GroundObject> layer = Map.of(new Position(0, 0), new GroundObject(1));
            final AttemptDTO dto = new AttemptDTO(
                    layer, new Position(0, 0), ZonedDateTime.now(), Duration.ZERO, false);
            final boolean result = levelService.validateLevelSubmission(testLevel, dto);
            assertFalse(result);
        }
    }

    /**
     * Tests for the submitAttempt method.
     */
    @Nested
    @DisplayName("when submitting an attempt")
    @SuppressWarnings("PMD.AtLeastOneConstructor")
    /* default */ class SubmitAttempt {

        /**
         * Verifies that submitting on a published level returns a success message.
         */
        @Test
        @DisplayName("should return a success message for a published level")
        /* default */ void testSubmitAttemptSuccess() {
            final Level mockLevel = Mockito.mock(Level.class);
            Mockito.lenient().when(mockLevel.isPublished()).thenReturn(true);
            when(userService.getById(USER_ID)).thenReturn(Optional.of(testUser));
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(mockLevel));
            final AttemptDTO dto = new AttemptDTO(
                    Map.of(), new Position(0, 0), ZonedDateTime.now(), Duration.ZERO, true);
            final String result = levelService.submitAttempt(LEVEL_ID, USER_ID, dto);
            assertEquals(SUCCESS_MSG, result);
        }

        /**
         * Verifies that UserNotFoundException is thrown when user does not exist.
         */
        @Test
        @DisplayName("should throw UserNotFoundException when user does not exist")
        /* default */ void testSubmitAttemptUserNotFound() {
            when(userService.getById(USER_ID)).thenReturn(Optional.empty());
            final AttemptDTO dto = new AttemptDTO(
                    Map.of(), new Position(0, 0), ZonedDateTime.now(), Duration.ZERO, true);
            assertThrows(UserNotFoundException.class,
                    () -> levelService.submitAttempt(LEVEL_ID, USER_ID, dto));
        }

        /**
         * Verifies that LevelNotFoundException is thrown when level does not exist.
         */
        @Test
        @DisplayName("should throw LevelNotFoundException when level does not exist")
        /* default */ void testSubmitAttemptLevelNotFound() {
            when(userService.getById(USER_ID)).thenReturn(Optional.of(testUser));
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.empty());
            final AttemptDTO dto = new AttemptDTO(
                    Map.of(), new Position(0, 0), ZonedDateTime.now(), Duration.ZERO, true);
            assertThrows(LevelNotFoundException.class,
                    () -> levelService.submitAttempt(LEVEL_ID, USER_ID, dto));
        }
    }
}
