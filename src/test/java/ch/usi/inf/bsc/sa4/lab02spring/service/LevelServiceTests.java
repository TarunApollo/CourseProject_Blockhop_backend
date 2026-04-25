package ch.usi.inf.bsc.sa4.lab02spring.service;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.AttemptDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.CloneLevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.CreateLevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.LevelSummaryDto;
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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Unit tests for the LevelService.
 * Goal: Test service logic in isolation using Mockito.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("The Level Service (Unit)")
@SuppressWarnings({ "PMD.AtLeastOneConstructor", "NullAway", "PMD.ExcessiveImports" })
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

    /** The service under test. */
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

    /** Shared display name for level-not-found tests. */
    private static final String LEVEL_NOT_FOUND_DISPLAY = "should throw LevelNotFoundException when level does not exist";

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
        this.levelService = new LevelService(
                levelRepository,
                userService,
                attemptRepository,
                Mockito.mock(LevelThumbnailRepository.class),
                Mockito.mock(ThumbnailRepository.class),
                userRepository,
                Mockito.mock(AttemptService.class),
                Mockito.mock(TileSetService.class),
                Mockito.mock(LayerToTiledMapConverter.class));
    }

    /**
     * Tests for the createLevel method.
     */
    @Nested
    @DisplayName("when creating a level")
    /* default */ class CreateLevel {

        /**
         * Verifies that a new level is saved and returned.
         */
        @Test
        @DisplayName("should save and return a new level for an existing user")
        /* default */ void testCreateLevelReturnsLevel() {
            final CreateLevelDTO dto = new CreateLevelDTO(LEVEL_TITLE, LEVEL_DESC);
            Mockito.when(userService.getById(USER_ID)).thenReturn(Optional.of(testUser));
            Mockito.when(levelRepository.save(Mockito.any(Level.class))).thenReturn(testLevel);
            final Level result = levelService.createLevel(dto, USER_ID);
            Assertions.assertNotNull(result);
        }

        /**
         * Verifies that UserNotFoundException is thrown when user is not found.
         */
        @Test
        @DisplayName("should throw UserNotFoundException when user does not exist")
        /* default */ void testCreateLevelUserNotFound() {
            final CreateLevelDTO dto = new CreateLevelDTO(LEVEL_TITLE, LEVEL_DESC);
            Mockito.when(userService.getById(USER_ID)).thenReturn(Optional.empty());
            Assertions.assertThrows(UserNotFoundException.class, () -> levelService.createLevel(dto, USER_ID));
        }
    }

    /**
     * Tests for the cloneLevel method.
     */
    @Nested
    @DisplayName("when cloning a level")
    /* default */ class CloneLevel {

        /**
         * Verifies that a level owned by the user can be cloned.
         */
        @Test
        @DisplayName("should return a non-empty optional when the level is owned by the user")
        /* default */ void testCloneLevelOwned() {
            final CloneLevelDTO dto = new CloneLevelDTO(LEVEL_ID);
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(testLevel));
            Mockito.when(levelRepository.findByCreator(testUser)).thenReturn(List.of());
            Mockito.when(levelRepository.save(Mockito.any(Level.class))).thenReturn(testLevel);
            final Optional<Level> result = levelService.cloneLevel(dto, testUser);
            Assertions.assertTrue(result.isPresent());
        }

        /**
         * Verifies that a level not owned by the user returns empty.
         */
        @Test
        @DisplayName("should return empty optional when the level is not owned by the user")
        /* default */ void testCloneLevelNotOwned() {
            final User otherUser = new User("user-2", "Luigi");
            final CloneLevelDTO dto = new CloneLevelDTO(LEVEL_ID);
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(testLevel));
            final Optional<Level> result = levelService.cloneLevel(dto, otherUser);
            Assertions.assertTrue(result.isEmpty());
        }
    }

    /**
     * Tests for the deleteLevel method.
     */
    @Nested
    @DisplayName("when deleting a level")
    /* default */ class DeleteLevel {

        /**
         * Verifies that an owned unpublished level is deleted.
         */
        @Test
        @DisplayName("should delete an owned unpublished level")
        /* default */ void testDeleteLevel() {
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(testLevel));
            levelService.deleteLevel(USER_ID, LEVEL_ID);
            Mockito.verify(levelRepository).deleteById(LEVEL_ID);
        }

        /**
         * Verifies that LevelNotFoundException is thrown when level does not exist.
         */
        @Test
        @DisplayName(LEVEL_NOT_FOUND_DISPLAY)
        /* default */ void testDeleteLevelNotFound() {
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.empty());
            Assertions.assertThrows(LevelNotFoundException.class, () -> levelService.deleteLevel(USER_ID, LEVEL_ID));
        }
    }

    /**
     * Tests for the updateLevelProperties method.
     */
    @Nested
    @DisplayName("when updating level properties")
    /* default */ class UpdateLevelProperties {

        /**
         * Verifies that the updated level is saved and returned.
         */
        @Test
        @DisplayName("should save and return the updated level")
        /* default */ void testUpdateLevelReturnsLevel() {
            final UpdateLevelDTO dto = new UpdateLevelDTO(Optional.empty(), Optional.empty(), Optional.empty());
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(testLevel));
            Mockito.when(levelRepository.save(testLevel)).thenReturn(testLevel);
            final Level result = levelService.updateLevelProperties(testUser, LEVEL_ID, dto);
            Assertions.assertNotNull(result);
        }

        /**
         * Verifies that LevelNotFoundException is thrown when level does not exist.
         */
        @Test
        @DisplayName(LEVEL_NOT_FOUND_DISPLAY)
        /* default */ void testUpdateLevelNotFound() {
            final UpdateLevelDTO dto = new UpdateLevelDTO(Optional.empty(), Optional.empty(), Optional.empty());
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.empty());
            Assertions.assertThrows(LevelNotFoundException.class,
                    () -> levelService.updateLevelProperties(testUser, LEVEL_ID, dto));
        }
    }

    /**
     * Tests for the publish method.
     */
    @Nested
    @DisplayName("when publishing a level")
    /* default */ class Publish {

        /**
         * Verifies that a level is published and returned.
         */
        @Test
        @DisplayName("should publish and return the level")
        /* default */ void testPublishReturnsLevel() {
            final Level mockLevel = Mockito.mock(Level.class);
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(mockLevel));
            Mockito.when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
            Mockito.when(levelRepository.save(mockLevel)).thenReturn(mockLevel);
            final Level result = levelService.publish(USER_ID, LEVEL_ID);
            Assertions.assertEquals(mockLevel, result);
        }

        /**
         * Verifies that publish is called on the level domain object.
         */
        @Test
        @DisplayName("should call publish on the domain level")
        /* default */ void testPublishCallsDomainMethod() {
            final Level mockLevel = Mockito.mock(Level.class);
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(mockLevel));
            Mockito.when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
            Mockito.when(levelRepository.save(mockLevel)).thenReturn(mockLevel);
            levelService.publish(USER_ID, LEVEL_ID);
            Mockito.verify(mockLevel).publish(USER_ID);
        }

        /**
         * Verifies that LevelNotFoundException is thrown when level does not exist.
         */
        @Test
        @DisplayName(LEVEL_NOT_FOUND_DISPLAY)
        /* default */ void testPublishLevelNotFound() {
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.empty());
            Assertions.assertThrows(LevelNotFoundException.class, () -> levelService.publish(USER_ID, LEVEL_ID));
        }

        /**
         * Verifies that UserNotFoundException is thrown when user does not exist.
         */
        @Test
        @DisplayName("should throw UserNotFoundException when user does not exist")
        /* default */ void testPublishUserNotFound() {
            final Level mockLevel = Mockito.mock(Level.class);
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(mockLevel));
            Mockito.when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());
            Assertions.assertThrows(UserNotFoundException.class, () -> levelService.publish(USER_ID, LEVEL_ID));
        }
    }

    /**
     * Tests for the unpublishLevel method.
     */
    @Nested
    @DisplayName("when unpublishing a level")
    /* default */ class UnpublishLevel {

        /**
         * Verifies that the unpublished level is returned.
         */
        @Test
        @DisplayName("should return the unpublished level")
        /* default */ void testUnpublishReturnsLevel() {
            final Level mockLevel = Mockito.mock(Level.class);
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(mockLevel));
            Mockito.when(levelRepository.save(mockLevel)).thenReturn(mockLevel);
            final Level result = levelService.unpublishLevel(USER_ID, LEVEL_ID);
            Assertions.assertNotNull(result);
        }
    }

    /**
     * Tests for the getPublishedLevels method.
     */
    @Nested
    @DisplayName("when getting published levels")
    /* default */ class GetPublishedLevels {

        /**
         * Verifies that published levels are returned sorted by clear rate.
         */
        @Test
        @DisplayName("should return published levels sorted by clear rate")
        /* default */ void testGetPublishedLevelsClearRate() {
            Mockito.when(levelRepository.findByPublishedTrue()).thenReturn(List.of(testLevel));
            Mockito.when(attemptRepository.countByLevel(testLevel)).thenReturn(10L);
            Mockito.when(attemptRepository.countByLevelAndCompletedTrue(testLevel)).thenReturn(5L);
            final List<LevelSummaryDto> result = levelService.getPublishedLevels(
                    PublishedLevelSortBy.CLEAR_RATE, DateRangePreset.AllTimeDateRangePreset.ALL_TIME);
            Assertions.assertEquals(1, result.size());
        }
    }

    /**
     * Tests for the getById method.
     */
    @Nested
    @DisplayName("when getting a level by ID")
    /* default */ class GetById {

        /**
         * Verifies that an existing level is found.
         */
        @Test
        @DisplayName("should return a non-empty optional when the level exists")
        /* default */ void testGetByIdFound() {
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(testLevel));
            final Optional<Level> result = levelService.getById(LEVEL_ID);
            Assertions.assertTrue(result.isPresent());
        }

        /**
         * Verifies that an empty optional is returned for unknown IDs.
         */
        @Test
        @DisplayName("should return an empty optional when the level does not exist")
        /* default */ void testGetByIdNotFound() {
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.empty());
            final Optional<Level> result = levelService.getById(LEVEL_ID);
            Assertions.assertTrue(result.isEmpty());
        }
    }

    /**
     * Tests for the validateLevelSubmission method.
     */
    @Nested
    @DisplayName("when validating a level submission")
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
            Assertions.assertTrue(result);
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
            Assertions.assertFalse(result);
        }
    }

    /**
     * Tests for the submitAttempt method.
     */
    @Nested
    @DisplayName("when submitting an attempt")
    /* default */ class SubmitAttempt {

        /**
         * Verifies that submitting on a published level returns a success message.
         */
        @Test
        @DisplayName("should return a success message for a published level")
        /* default */ void testSubmitAttemptSuccess() {
            final Level mockLevel = Mockito.mock(Level.class);
            Mockito.lenient().when(mockLevel.isPublished()).thenReturn(Boolean.TRUE);
            Mockito.when(userService.getById(USER_ID)).thenReturn(Optional.of(testUser));
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(mockLevel));
            final AttemptDTO dto = new AttemptDTO(
                    Map.of(), new Position(0, 0), ZonedDateTime.now(), Duration.ZERO, true);
            final String result = levelService.submitAttempt(LEVEL_ID, USER_ID, dto);
            Assertions.assertEquals(SUCCESS_MSG, result);
        }

        /**
         * Verifies that UserNotFoundException is thrown when user does not exist.
         */
        @Test
        @DisplayName("should throw UserNotFoundException when user does not exist")
        /* default */ void testSubmitAttemptUserNotFound() {
            Mockito.when(userService.getById(USER_ID)).thenReturn(Optional.empty());
            final AttemptDTO dto = new AttemptDTO(
                    Map.of(), new Position(0, 0), ZonedDateTime.now(), Duration.ZERO, true);
            Assertions.assertThrows(UserNotFoundException.class,
                    () -> levelService.submitAttempt(LEVEL_ID, USER_ID, dto));
        }

        /**
         * Verifies that LevelNotFoundException is thrown when level does not exist.
         */
        @Test
        @DisplayName(LEVEL_NOT_FOUND_DISPLAY)
        /* default */ void testSubmitAttemptLevelNotFound() {
            Mockito.when(userService.getById(USER_ID)).thenReturn(Optional.of(testUser));
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.empty());
            final AttemptDTO dto = new AttemptDTO(
                    Map.of(), new Position(0, 0), ZonedDateTime.now(), Duration.ZERO, true);
            Assertions.assertThrows(LevelNotFoundException.class,
                    () -> levelService.submitAttempt(LEVEL_ID, USER_ID, dto));
        }
    }
}