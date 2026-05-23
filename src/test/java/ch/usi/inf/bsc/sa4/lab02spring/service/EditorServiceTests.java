package ch.usi.inf.bsc.sa4.lab02spring.service;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.BoxPropertyUpdateDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.EditorLevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.UpdateObjectLayerDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.UpdateWorldLayerDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.Box;
import ch.usi.inf.bsc.sa4.lab02spring.model.ClearCondition;
import ch.usi.inf.bsc.sa4.lab02spring.model.CoinType;
import ch.usi.inf.bsc.sa4.lab02spring.model.Condition;
import ch.usi.inf.bsc.sa4.lab02spring.model.Content;
import ch.usi.inf.bsc.sa4.lab02spring.model.ExitDoor;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.Position;
import ch.usi.inf.bsc.sa4.lab02spring.model.StartFlag;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.repository.LevelRepository;
import ch.usi.inf.bsc.sa4.lab02spring.service.level.LevelPublishService;
import ch.usi.inf.bsc.sa4.lab02spring.utils.ForbiddenUserException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.LevelNotFoundException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.LevelPublishedException;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
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

/// Tests for [EditorService].
@SpringBootTest
@DisplayName("Editor Service")
@SuppressWarnings("PMD.ExcessiveImports")
class EditorServiceTests {

    /// Owner ID.
    private static final String OWNER_ID = "user-1";

    /// Other user ID.
    private static final String OTHER_USER_ID = "user-2";

    /// Level ID.
    private static final String LEVEL_ID = "level-1";

    /// Owner name.
    private static final String OWNER_NAME = "Mario";

    /// Level title.
    private static final String LEVEL_TITLE = "Test Level";

    /// Level description.
    private static final String LEVEL_DESC = "Test Description";

    /// Ground tile ID.
    private static final String GROUND_TILE_ID = "terrain.grass.block";

    /// Object tile ID A.
    private static final String OBJECT_TILE_ID_A = "block.plank";

    /// Object tile ID B.
    private static final String OBJECT_TILE_ID_B = "decoration.mushroom.red";

    /// Box tile ID.
    private static final String BOX_TILE_ID = "block.plank";

    /// Invalid tile ID.
    private static final String INVALID_TILE_ID = "missing.tile";

    /// Position A.
    private static final Position POS_A = new Position(0, 0);

    /// Position B.
    private static final Position POS_B = new Position(1, 0);

    /// Box position.
    private static final Position BOX_POS = new Position(2, 0);

    /// Out of bounds position.
    private static final Position OUT_OF_BOUNDS_POS = new Position(0, 14);

    /// Service under test.
    @Autowired
    private EditorService editorService;

    /// Mock repository.
    @MockitoBean
    private LevelRepository levelRepository;

    /// Mock catalog service.
    @MockitoBean
    private TileCatalogService tileCatalogService;

    /// Mock factory.
    @MockitoBean
    private GameObjectFactory gameObjectFactory;

    /// Mock publish service.
    @MockitoBean
    private LevelPublishService levelPublishService;

    /// Test level.
    private Level testLevel;

    /// Published level.
    private Level publishedLevel;

    /// Setup test data.
    @BeforeEach
    void setup() {
        final User testUser = new User(OWNER_ID, OWNER_NAME);
        this.testLevel = new Level(LEVEL_TITLE, LEVEL_DESC, testUser);
        this.publishedLevel = new Level(
                testUser, LEVEL_TITLE, LEVEL_DESC, true,
                new ClearCondition(new Condition.NoClearCondition(), 0),
                Map.of(), Map.of());
    }

    /// Tests for [EditorService.replaceWorldLayer].
    @Nested
    @DisplayName("world layer replacement")
    class ReplaceWorldLayer {

        /// Check if level is saved and state reset.
        @Test
        @DisplayName("saves level and resets state on empty replacement")
        void savesEmptyLayerAndInvalidatesPublish() {
            final UpdateWorldLayerDTO dto = new UpdateWorldLayerDTO(List.of());
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(testLevel));
            Mockito.when(levelRepository.save(testLevel)).thenReturn(testLevel);

            final Level result = editorService.replaceWorldLayer(OWNER_ID, LEVEL_ID, dto);

            Assertions.assertSame(testLevel, result);
            Mockito.verify(levelRepository).save(testLevel);
            Mockito.verify(levelPublishService).resetLevelAfterEdit(testLevel, OWNER_ID);
        }

        /// Check if ground tile is placed.
        @Test
        @DisplayName("places ground tile in world layer")
        void replacesWorldLayerWithGroundTile() {
            final EditorLevelDTO tile = EditorLevelDTO.create(POS_A, GROUND_TILE_ID);
            final UpdateWorldLayerDTO dto = new UpdateWorldLayerDTO(List.of(tile));
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(testLevel));
            Mockito.when(tileCatalogService.isWorldTile(GROUND_TILE_ID)).thenReturn(Boolean.TRUE);
            Mockito.when(levelRepository.save(testLevel)).thenReturn(testLevel);

            editorService.replaceWorldLayer(OWNER_ID, LEVEL_ID, dto);

            Assertions.assertNotNull(testLevel.getWorldLayer().get(POS_A));
        }

        /// Check if out of bounds position is rejected.
        @Test
        @DisplayName("fails on out of bounds position")
        void throwsOnOutOfBoundsPosition() {
            final EditorLevelDTO tile = EditorLevelDTO.create(OUT_OF_BOUNDS_POS, GROUND_TILE_ID);
            final UpdateWorldLayerDTO dto = new UpdateWorldLayerDTO(List.of(tile));
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(testLevel));

            Assertions.assertThrows(IllegalArgumentException.class,
                    () -> editorService.replaceWorldLayer(OWNER_ID, LEVEL_ID, dto));

            Mockito.verify(levelRepository, Mockito.never()).save(Mockito.any());
        }

        /// Check if invalid tile ID is rejected.
        @Test
        @DisplayName("fails on invalid tile ID")
        void throwsOnInvalidGroundGid() {
            final EditorLevelDTO tile = EditorLevelDTO.create(POS_A, INVALID_TILE_ID);
            final UpdateWorldLayerDTO dto = new UpdateWorldLayerDTO(List.of(tile));
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(testLevel));
            Mockito.when(tileCatalogService.isWorldTile(INVALID_TILE_ID)).thenReturn(Boolean.FALSE);

            Assertions.assertThrows(IllegalArgumentException.class,
                    () -> editorService.replaceWorldLayer(OWNER_ID, LEVEL_ID, dto));

            Mockito.verify(levelRepository, Mockito.never()).save(Mockito.any());
        }

        /// Check if null tile ID is rejected.
        @Test
        @DisplayName("fails on null tile ID")
        @SuppressWarnings("NullAway")
        void throwsOnNullGroundGid() {
            final EditorLevelDTO tile = EditorLevelDTO.create(POS_A, null);
            final UpdateWorldLayerDTO dto = new UpdateWorldLayerDTO(List.of(tile));
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(testLevel));

            Assertions.assertThrows(IllegalArgumentException.class,
                    () -> editorService.replaceWorldLayer(OWNER_ID, LEVEL_ID, dto));

            Mockito.verify(levelRepository, Mockito.never()).save(Mockito.any());
        }

        /// Check if blank tile ID is rejected.
        @Test
        @DisplayName("fails on blank tile ID")
        void throwsOnBlankGroundGid() {
            final EditorLevelDTO tile = EditorLevelDTO.create(POS_A, "   ");
            final UpdateWorldLayerDTO dto = new UpdateWorldLayerDTO(List.of(tile));
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(testLevel));

            Assertions.assertThrows(IllegalArgumentException.class,
                    () -> editorService.replaceWorldLayer(OWNER_ID, LEVEL_ID, dto));

            Mockito.verify(levelRepository, Mockito.never()).save(Mockito.any());
        }

        /// Check if missing level fails.
        @Test
        @DisplayName("fails if level is missing")
        void throwsLevelNotFoundAndAbortsSave() {
            final UpdateWorldLayerDTO dto = new UpdateWorldLayerDTO(List.of());
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.empty());

            Assertions.assertThrows(LevelNotFoundException.class,
                    () -> editorService.replaceWorldLayer(OWNER_ID, LEVEL_ID, dto));

            Mockito.verify(levelRepository, Mockito.never()).save(Mockito.any());
        }

        /// Check if non-owner fails.
        @Test
        @DisplayName("fails if user is not the owner")
        void throwsForbiddenAndAbortsSave() {
            final UpdateWorldLayerDTO dto = new UpdateWorldLayerDTO(List.of());
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(testLevel));

            Assertions.assertThrows(ForbiddenUserException.class,
                    () -> editorService.replaceWorldLayer(OTHER_USER_ID, LEVEL_ID, dto));

            Mockito.verify(levelRepository, Mockito.never()).save(Mockito.any());
        }

        /// Check if published level fails.
        @Test
        @DisplayName("fails if level is published")
        void throwsPublishedAndAbortsSave() {
            final UpdateWorldLayerDTO dto = new UpdateWorldLayerDTO(List.of());
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(publishedLevel));

            Assertions.assertThrows(LevelPublishedException.class,
                    () -> editorService.replaceWorldLayer(OWNER_ID, LEVEL_ID, dto));

            Mockito.verify(levelRepository, Mockito.never()).save(Mockito.any());
        }
    }

    /// Tests for [EditorService.replaceObjectLayer].
    @Nested
    @DisplayName("object layer replacement")
    class ReplaceObjectLayer {

        /// Check if level is saved and state reset.
        @Test
        @DisplayName("saves level and resets state on empty replacement")
        void savesEmptyLayerAndInvalidatesPublish() {
            final UpdateObjectLayerDTO dto = new UpdateObjectLayerDTO(List.of());
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(testLevel));
            Mockito.when(levelRepository.save(testLevel)).thenReturn(testLevel);

            final Level result = editorService.replaceObjectLayer(OWNER_ID, LEVEL_ID, dto);

            Assertions.assertSame(testLevel, result);
            Mockito.verify(levelRepository).save(testLevel);
            Mockito.verify(levelPublishService).resetLevelAfterEdit(testLevel, OWNER_ID);
        }

        /// Check if objects are placed via factory.
        @Test
        @DisplayName("places objects via factory and saves")
        void placesTwoObjectsViaFactoryAndSaves() {
            final EditorLevelDTO obj1 = EditorLevelDTO.create(POS_A, OBJECT_TILE_ID_A);
            final EditorLevelDTO obj2 = EditorLevelDTO.create(POS_B, OBJECT_TILE_ID_B);
            final UpdateObjectLayerDTO dto = new UpdateObjectLayerDTO(List.of(obj1, obj2));
            final Box realObjA = new Box(OBJECT_TILE_ID_A, POS_A, new Content.NoContent());
            final Box realObjB = new Box(OBJECT_TILE_ID_B, POS_B, new Content.NoContent());
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(testLevel));
            Mockito.when(tileCatalogService.isObjectTile(OBJECT_TILE_ID_A)).thenReturn(Boolean.TRUE);
            Mockito.when(tileCatalogService.isObjectTile(OBJECT_TILE_ID_B)).thenReturn(Boolean.TRUE);
            Mockito.when(gameObjectFactory.createGameObject(OBJECT_TILE_ID_A, POS_A, new Content.NoContent()))
                    .thenReturn(realObjA);
            Mockito.when(gameObjectFactory.createGameObject(OBJECT_TILE_ID_B, POS_B, new Content.NoContent()))
                    .thenReturn(realObjB);
            Mockito.when(levelRepository.save(testLevel)).thenReturn(testLevel);

            editorService.replaceObjectLayer(OWNER_ID, LEVEL_ID, dto);

            Assertions.assertSame(realObjA, Objects.requireNonNull(testLevel.getObjectLayer().get(POS_A)));
            Assertions.assertSame(realObjB, Objects.requireNonNull(testLevel.getObjectLayer().get(POS_B)));
            Mockito.verify(levelRepository).save(testLevel);
        }

        /// Check if duplicate positions fail.
        @Test
        @DisplayName("fails on duplicate object position")
        void throwsOnDuplicatePositionAndAbortsSave() {
            final EditorLevelDTO obj1 = EditorLevelDTO.create(POS_A, OBJECT_TILE_ID_A);
            final EditorLevelDTO obj2 = EditorLevelDTO.create(POS_A, OBJECT_TILE_ID_B);
            final UpdateObjectLayerDTO dto = new UpdateObjectLayerDTO(List.of(obj1, obj2));
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(testLevel));
            Mockito.when(tileCatalogService.isObjectTile(OBJECT_TILE_ID_A)).thenReturn(Boolean.TRUE);
            Mockito.when(tileCatalogService.isObjectTile(OBJECT_TILE_ID_B)).thenReturn(Boolean.TRUE);

            Assertions.assertThrows(IllegalArgumentException.class,
                    () -> editorService.replaceObjectLayer(OWNER_ID, LEVEL_ID, dto));

            Mockito.verify(levelRepository, Mockito.never()).save(Mockito.any());
        }

        /// Check if out of bounds position fails.
        @Test
        @DisplayName("fails on out of bounds position")
        void throwsOnOutOfBoundsPosition() {
            final EditorLevelDTO obj = EditorLevelDTO.create(OUT_OF_BOUNDS_POS, OBJECT_TILE_ID_A);
            final UpdateObjectLayerDTO dto = new UpdateObjectLayerDTO(List.of(obj));
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(testLevel));

            Assertions.assertThrows(IllegalArgumentException.class,
                    () -> editorService.replaceObjectLayer(OWNER_ID, LEVEL_ID, dto));

            Mockito.verify(levelRepository, Mockito.never()).save(Mockito.any());
        }

        /// Check if invalid tile ID fails.
        @Test
        @DisplayName("fails on invalid tile ID")
        void throwsOnInvalidObjectGid() {
            final EditorLevelDTO obj = EditorLevelDTO.create(POS_A, INVALID_TILE_ID);
            final UpdateObjectLayerDTO dto = new UpdateObjectLayerDTO(List.of(obj));
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(testLevel));
            Mockito.when(tileCatalogService.isObjectTile(INVALID_TILE_ID)).thenReturn(Boolean.FALSE);

            Assertions.assertThrows(IllegalArgumentException.class,
                    () -> editorService.replaceObjectLayer(OWNER_ID, LEVEL_ID, dto));

            Mockito.verify(levelRepository, Mockito.never()).save(Mockito.any());
        }

        /// Check if multiple start flags fail.
        @Test
        @DisplayName("fails if two start flags are placed")
        void throwsWhenTwoStartFlagsPlaced() {
            final EditorLevelDTO obj1 = EditorLevelDTO.create(POS_A, OBJECT_TILE_ID_A);
            final EditorLevelDTO obj2 = EditorLevelDTO.create(POS_B, OBJECT_TILE_ID_B);
            final UpdateObjectLayerDTO dto = new UpdateObjectLayerDTO(List.of(obj1, obj2));
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(testLevel));
            Mockito.when(tileCatalogService.isObjectTile(OBJECT_TILE_ID_A)).thenReturn(Boolean.TRUE);
            Mockito.when(tileCatalogService.isObjectTile(OBJECT_TILE_ID_B)).thenReturn(Boolean.TRUE);
            Mockito.when(gameObjectFactory.createGameObject(OBJECT_TILE_ID_A, POS_A, new Content.NoContent()))
                    .thenReturn(new StartFlag(OBJECT_TILE_ID_A, POS_A));
            Mockito.when(gameObjectFactory.createGameObject(OBJECT_TILE_ID_B, POS_B, new Content.NoContent()))
                    .thenReturn(new StartFlag(OBJECT_TILE_ID_B, POS_B));

            Assertions.assertThrows(IllegalArgumentException.class,
                    () -> editorService.replaceObjectLayer(OWNER_ID, LEVEL_ID, dto));

            Mockito.verify(levelRepository, Mockito.never()).save(Mockito.any());
        }

        /// Check if multiple exit doors fail.
        @Test
        @DisplayName("fails if two exit doors are placed")
        void throwsWhenTwoExitDoorsPlaced() {
            final EditorLevelDTO obj1 = EditorLevelDTO.create(POS_A, OBJECT_TILE_ID_A);
            final EditorLevelDTO obj2 = EditorLevelDTO.create(POS_B, OBJECT_TILE_ID_B);
            final UpdateObjectLayerDTO dto = new UpdateObjectLayerDTO(List.of(obj1, obj2));
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(testLevel));
            Mockito.when(tileCatalogService.isObjectTile(OBJECT_TILE_ID_A)).thenReturn(Boolean.TRUE);
            Mockito.when(tileCatalogService.isObjectTile(OBJECT_TILE_ID_B)).thenReturn(Boolean.TRUE);
            Mockito.when(gameObjectFactory.createGameObject(OBJECT_TILE_ID_A, POS_A, new Content.NoContent()))
                    .thenReturn(new ExitDoor(OBJECT_TILE_ID_A, POS_A));
            Mockito.when(gameObjectFactory.createGameObject(OBJECT_TILE_ID_B, POS_B, new Content.NoContent()))
                    .thenReturn(new ExitDoor(OBJECT_TILE_ID_B, POS_B));

            Assertions.assertThrows(IllegalArgumentException.class,
                    () -> editorService.replaceObjectLayer(OWNER_ID, LEVEL_ID, dto));

            Mockito.verify(levelRepository, Mockito.never()).save(Mockito.any());
        }

        /// Check if missing level fails.
        @Test
        @DisplayName("fails if level is missing")
        void throwsLevelNotFoundAndAbortsSave() {
            final UpdateObjectLayerDTO dto = new UpdateObjectLayerDTO(List.of());
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.empty());

            Assertions.assertThrows(LevelNotFoundException.class,
                    () -> editorService.replaceObjectLayer(OWNER_ID, LEVEL_ID, dto));

            Mockito.verify(levelRepository, Mockito.never()).save(Mockito.any());
        }

        /// Check if non-owner fails.
        @Test
        @DisplayName("fails if user is not the owner")
        void throwsForbiddenAndAbortsSave() {
            final UpdateObjectLayerDTO dto = new UpdateObjectLayerDTO(List.of());
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(testLevel));

            Assertions.assertThrows(ForbiddenUserException.class,
                    () -> editorService.replaceObjectLayer(OTHER_USER_ID, LEVEL_ID, dto));

            Mockito.verify(levelRepository, Mockito.never()).save(Mockito.any());
        }

        /// Check if published level fails.
        @Test
        @DisplayName("fails if level is published")
        void throwsPublishedAndAbortsSave() {
            final UpdateObjectLayerDTO dto = new UpdateObjectLayerDTO(List.of());
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(publishedLevel));

            Assertions.assertThrows(LevelPublishedException.class,
                    () -> editorService.replaceObjectLayer(OWNER_ID, LEVEL_ID, dto));

            Mockito.verify(levelRepository, Mockito.never()).save(Mockito.any());
        }
    }

    /// Tests for [EditorService.updateObjectProperties].
    @Nested
    @DisplayName("object property updates")
    class UpdateObjectProperties {

        /// Check if box content is updated.
        @Test
        @DisplayName("updates box content and saves level")
        void updatesBoxContentAndSaves() {
            final Content newContent = new Content.SomeContent(CoinType.GOLD_COIN);
            testLevel.putObjectLayer(BOX_POS, new Box(BOX_TILE_ID, BOX_POS, new Content.NoContent()));
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(testLevel));
            Mockito.when(levelRepository.save(testLevel)).thenReturn(testLevel);

            editorService.updateObjectProperties(OWNER_ID, LEVEL_ID, new BoxPropertyUpdateDTO(BOX_POS, newContent));

            final Box updated = Assertions.assertInstanceOf(Box.class,
                    Objects.requireNonNull(testLevel.getObjectLayer().get(BOX_POS)));
            Assertions.assertEquals(newContent, updated.content());
            Mockito.verify(levelRepository).save(testLevel);
            Mockito.verify(levelPublishService).resetLevelAfterEdit(testLevel, OWNER_ID);
        }

        /// Check if missing object fails.
        @Test
        @DisplayName("fails if no object exists at position")
        void throwsWhenNoObjectAtPosition() {
            final BoxPropertyUpdateDTO dto = new BoxPropertyUpdateDTO(BOX_POS, new Content.NoContent());
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(testLevel));

            Assertions.assertThrows(NoSuchElementException.class,
                    () -> editorService.updateObjectProperties(OWNER_ID, LEVEL_ID, dto));

            Mockito.verify(levelRepository, Mockito.never()).save(Mockito.any());
        }

        /// Check if non-box fails.
        @Test
        @DisplayName("fails if object at position is not a Box")
        void throwsWhenNonBoxAtPosition() {
            testLevel.putObjectLayer(BOX_POS, new StartFlag(BOX_TILE_ID, BOX_POS));
            final BoxPropertyUpdateDTO dto = new BoxPropertyUpdateDTO(BOX_POS, new Content.NoContent());
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(testLevel));

            Assertions.assertThrows(IllegalArgumentException.class,
                    () -> editorService.updateObjectProperties(OWNER_ID, LEVEL_ID, dto));

            Mockito.verify(levelRepository, Mockito.never()).save(Mockito.any());
        }

        /// Check if out of bounds fails.
        @Test
        @DisplayName("fails on out of bounds position")
        void throwsOnOutOfBoundsPosition() {
            final BoxPropertyUpdateDTO dto = new BoxPropertyUpdateDTO(OUT_OF_BOUNDS_POS, new Content.NoContent());
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(testLevel));

            Assertions.assertThrows(IllegalArgumentException.class,
                    () -> editorService.updateObjectProperties(OWNER_ID, LEVEL_ID, dto));

            Mockito.verify(levelRepository, Mockito.never()).save(Mockito.any());
        }

        /// Check if unsupported object property update fails.
        @Test
        @DisplayName("fails on unsupported property update type")
        @SuppressWarnings("NullAway")
        void throwsOnUnsupportedPropertyUpdateType() {
            testLevel.putObjectLayer(BOX_POS, new StartFlag(BOX_TILE_ID, BOX_POS));
            
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(testLevel));

            Assertions.assertThrows(NullPointerException.class,
                    () -> editorService.updateObjectProperties(OWNER_ID, LEVEL_ID, null));

            Mockito.verify(levelRepository, Mockito.never()).save(Mockito.any());
        }

        /// Check if missing level fails.
        @Test
        @DisplayName("fails if level is missing")
        void throwsLevelNotFoundAndAbortsSave() {
            final BoxPropertyUpdateDTO dto = new BoxPropertyUpdateDTO(BOX_POS, new Content.NoContent());
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.empty());

            Assertions.assertThrows(LevelNotFoundException.class,
                    () -> editorService.updateObjectProperties(OWNER_ID, LEVEL_ID, dto));

            Mockito.verify(levelRepository, Mockito.never()).save(Mockito.any());
        }

        /// Check if non-owner fails.
        @Test
        @DisplayName("fails if user is not the owner")
        void throwsForbiddenAndAbortsSave() {
            final BoxPropertyUpdateDTO dto = new BoxPropertyUpdateDTO(BOX_POS, new Content.NoContent());
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(testLevel));

            Assertions.assertThrows(ForbiddenUserException.class,
                    () -> editorService.updateObjectProperties(OTHER_USER_ID, LEVEL_ID, dto));

            Mockito.verify(levelRepository, Mockito.never()).save(Mockito.any());
        }

        /// Check if published level fails.
        @Test
        @DisplayName("fails if level is published")
        void throwsPublishedAndAbortsSave() {
            final BoxPropertyUpdateDTO dto = new BoxPropertyUpdateDTO(BOX_POS, new Content.NoContent());
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(publishedLevel));

            Assertions.assertThrows(LevelPublishedException.class,
                    () -> editorService.updateObjectProperties(OWNER_ID, LEVEL_ID, dto));

            Mockito.verify(levelRepository, Mockito.never()).save(Mockito.any());
        }
    }
}
