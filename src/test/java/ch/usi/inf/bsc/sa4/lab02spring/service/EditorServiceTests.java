package ch.usi.inf.bsc.sa4.lab02spring.service;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.BoxPropertyUpdateDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.EditorLevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.UpdateObjectLayerDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.UpdateWorldLayerDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.Content;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.Position;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.repository.LevelRepository;
import ch.usi.inf.bsc.sa4.lab02spring.utils.LevelNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

/**
 * Unit tests for the EditorService.
 * Goal: Test layer editing logic in isolation using Mockito.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("The Editor Service (Unit)")
@SuppressWarnings({ "PMD.AtLeastOneConstructor", "NullAway" })
/* default */ class EditorServiceTests {

    /** The mocked level repository. */
    @Mock
    private LevelRepository levelRepository;

    /** The mocked tileset service. */
    @Mock
    private TileSetService tileSetService;

    /** The service under test. */
    private EditorService editorService;

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

    /** The test level owned by testUser and unpublished. */
    private Level testLevel;

    /**
     * Sets up test data before each test.
     */
    @BeforeEach
    /* default */ void setup() {
        final User testUser = new User(USER_ID, USER_NAME);
        this.testLevel = new Level(LEVEL_TITLE, LEVEL_DESC, testUser);
        this.editorService = new EditorService(
                levelRepository,
                tileSetService,
                Mockito.mock(GameObjectFactory.class),
                Mockito.mock(ch.usi.inf.bsc.sa4.lab02spring.service.level.LevelPublishService.class));
    }

    /**
     * Tests for the replaceWorldLayer method.
     */
    @Nested
    @DisplayName("when replacing the world layer")
    /* default */ class ReplaceWorldLayer {

        /**
         * Verifies that an empty tile list replaces the world layer and returns the level.
         */
        @Test
        @DisplayName("should return the updated level when the tile list is empty")
        /* default */ void testReplaceWorldLayerEmpty() {
            final UpdateWorldLayerDTO dto = new UpdateWorldLayerDTO(List.of());
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(testLevel));
            Mockito.when(levelRepository.save(testLevel)).thenReturn(testLevel);
            final Level result = editorService.replaceWorldLayer(USER_ID, LEVEL_ID, dto);
            Assertions.assertNotNull(result);
        }

        /**
         * Verifies that the level is persisted after replacement.
         */
        @Test
        @DisplayName("should save the level after replacing the world layer")
        /* default */ void testReplaceWorldLayerSaves() {
            final UpdateWorldLayerDTO dto = new UpdateWorldLayerDTO(List.of());
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(testLevel));
            Mockito.when(levelRepository.save(testLevel)).thenReturn(testLevel);
            editorService.replaceWorldLayer(USER_ID, LEVEL_ID, dto);
            Mockito.verify(levelRepository).save(testLevel);
        }

        /**
         * Verifies that LevelNotFoundException is thrown when level does not exist.
         */
        @Test
        @DisplayName("should throw LevelNotFoundException when level does not exist")
        /* default */ void testReplaceWorldLayerNotFound() {
            final UpdateWorldLayerDTO dto = new UpdateWorldLayerDTO(List.of());
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.empty());
            Assertions.assertThrows(LevelNotFoundException.class,
                    () -> editorService.replaceWorldLayer(USER_ID, LEVEL_ID, dto));
        }

        /**
         * Verifies that EditorLevelDTO.create can be used for a single tile.
         */
        @Test
        @DisplayName("should validate the tile position and GID for each tile in the list")
        /* default */ void testReplaceWorldLayerWithTile() {
            final EditorLevelDTO tile = EditorLevelDTO.create(new Position(0, 0), 1);
            final UpdateWorldLayerDTO dto = new UpdateWorldLayerDTO(List.of(tile));
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(testLevel));
            Mockito.when(tileSetService.isGroundGID(1)).thenReturn(Boolean.TRUE);
            Mockito.when(levelRepository.save(testLevel)).thenReturn(testLevel);
            final Level result = editorService.replaceWorldLayer(USER_ID, LEVEL_ID, dto);
            Assertions.assertNotNull(result);
        }
    }

    /**
     * Tests for the replaceObjectLayer method.
     */
    @Nested
    @DisplayName("when replacing the object layer")
    /* default */ class ReplaceObjectLayer {

        /**
         * Verifies that an empty object list replaces the object layer
         * and returns the level.
         */
        @Test
        @DisplayName("should return the updated level when the object list is empty")
        /* default */ void testReplaceObjectLayerEmpty() {
            final UpdateObjectLayerDTO dto = new UpdateObjectLayerDTO(List.of());
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(testLevel));
            Mockito.when(levelRepository.save(testLevel)).thenReturn(testLevel);
            final Level result = editorService.replaceObjectLayer(USER_ID, LEVEL_ID, dto);
            Assertions.assertNotNull(result);
        }

        /**
         * Verifies that the level is persisted after replacement.
         */
        @Test
        @DisplayName("should save the level after replacing the object layer")
        /* default */ void testReplaceObjectLayerSaves() {
            final UpdateObjectLayerDTO dto = new UpdateObjectLayerDTO(List.of());
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(testLevel));
            Mockito.when(levelRepository.save(testLevel)).thenReturn(testLevel);
            editorService.replaceObjectLayer(USER_ID, LEVEL_ID, dto);
            Mockito.verify(levelRepository).save(testLevel);
        }

        /**
         * Verifies that LevelNotFoundException is thrown when level does not exist.
         */
        @Test
        @DisplayName("should throw LevelNotFoundException when level does not exist")
        /* default */ void testReplaceObjectLayerNotFound() {
            final UpdateObjectLayerDTO dto = new UpdateObjectLayerDTO(List.of());
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.empty());
            Assertions.assertThrows(LevelNotFoundException.class,
                    () -> editorService.replaceObjectLayer(USER_ID, LEVEL_ID, dto));
        }
    }

    /**
     * Tests for the updateObjectProperties method.
     */
    @Nested
    @DisplayName("when updating object properties")
    /* default */ class UpdateObjectProperties {

        /**
         * Verifies that a BoxPropertyUpdateDTO updates the level and returns it.
         */
        @Test
        @DisplayName("should update box content and return the level")
        /* default */ void testUpdateBoxPropertiesReturnsLevel() {
            final Level mockLevel = Mockito.mock(Level.class);
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(mockLevel));
            Mockito.when(levelRepository.save(mockLevel)).thenReturn(mockLevel);
            final BoxPropertyUpdateDTO dto = new BoxPropertyUpdateDTO(
                    new Position(0, 0), new Content.NoContent());
            final Level result = editorService.updateObjectProperties(USER_ID, LEVEL_ID, dto);
            Assertions.assertEquals(mockLevel, result);
        }

        /**
         * Verifies that the level is persisted after property update.
         */
        @Test
        @DisplayName("should save the level after updating object properties")
        /* default */ void testUpdateBoxPropertiesSaves() {
            final Level mockLevel = Mockito.mock(Level.class);
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(mockLevel));
            Mockito.when(levelRepository.save(mockLevel)).thenReturn(mockLevel);
            final BoxPropertyUpdateDTO dto = new BoxPropertyUpdateDTO(
                    new Position(0, 0), new Content.NoContent());
            editorService.updateObjectProperties(USER_ID, LEVEL_ID, dto);
            Mockito.verify(levelRepository).save(mockLevel);
        }

        /**
         * Verifies that LevelNotFoundException is thrown when level does not exist.
         */
        @Test
        @DisplayName("should throw LevelNotFoundException when level does not exist")
        /* default */ void testUpdateObjectPropertiesNotFound() {
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.empty());
            final BoxPropertyUpdateDTO dto = new BoxPropertyUpdateDTO(
                    new Position(0, 0), new Content.NoContent());
            Assertions.assertThrows(LevelNotFoundException.class,
                    () -> editorService.updateObjectProperties(USER_ID, LEVEL_ID, dto));
        }
    }
}