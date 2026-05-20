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

/// Integration tests for [EditorService] using a mocked Spring context.
@SpringBootTest
@DisplayName("The Editor Service")
@SuppressWarnings("PMD.ExcessiveImports")
class EditorServiceTests {

    /// Owner's user ID used across tests.
    private static final String OWNER_ID = "user-1";

    /// ID of a different user who is not the level owner.
    private static final String OTHER_USER_ID = "user-2";

    /// Level ID used across tests.
    private static final String LEVEL_ID = "level-1";

    /// Display name of the test owner.
    private static final String OWNER_NAME = "Mario";

    /// Title of the test level.
    private static final String LEVEL_TITLE = "Test Level";

    /// Description of the test level.
    private static final String LEVEL_DESC = "Test Description";

    /// GID of a ground tile used in world layer tests.
    private static final int GROUND_GID = 1;

    /// GID of the first object tile used in object layer tests.
    private static final int OBJECT_GID_A = 42;

    /// GID of the second object tile used in object layer tests.
    private static final int OBJECT_GID_B = 43;

    /// GID of the box placed in property update tests.
    private static final int BOX_GID = 10;

    /// A GID value that the tileset service will not recognize as valid.
    private static final int INVALID_GID = 999;

    /// First tile position used in layer tests.
    private static final Position POS_A = new Position(0, 0);

    /// Second tile position used in multi-tile layer tests.
    private static final Position POS_B = new Position(1, 0);

    /// Position of the box placed in property update tests.
    private static final Position BOX_POS = new Position(2, 0);

    /// A position that lies outside the level's valid bounds
    /// (y exceeds max height of 13).
    private static final Position OUT_OF_BOUNDS_POS = new Position(0, 14);

    /// The service under test.
    @Autowired
    private EditorService editorService;

    /// Mocked level repository to isolate tests from the database.
    @MockitoBean
    private LevelRepository levelRepository;

    /// Mocked tileset service to control GID validation results.
    @MockitoBean
    private TileSetService tileSetService;

    /// Mocked factory to control game object creation.
    @MockitoBean
    private GameObjectFactory gameObjectFactory;

    /// Mocked publish service to verify side-effect calls.
    @MockitoBean
    private LevelPublishService levelPublishService;

    /// An unpublished level owned by the test user.
    private Level testLevel;

    /// A published level used for modifiability failure tests.
    private Level publishedLevel;

    /// Initializes shared test data before each test.
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
    @DisplayName("when replacing the world layer")
    class ReplaceWorldLayer {

        /// Verifies the level is saved
        /// and public publication state is reset on a successful empty-list replacement.
        @Test
        @DisplayName("saves the level and resets publication state with an empty tile list")
        void savesEmptyLayerAndInvalidatesPublish() {
            final UpdateWorldLayerDTO dto = new UpdateWorldLayerDTO(List.of());
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(testLevel));
            Mockito.when(levelRepository.save(testLevel)).thenReturn(testLevel);

            final Level result = editorService.replaceWorldLayer(OWNER_ID, LEVEL_ID, dto);

            Assertions.assertSame(testLevel, result);
            Mockito.verify(levelRepository).save(testLevel);
            Mockito.verify(levelPublishService).resetLevelAfterEdit(testLevel, OWNER_ID);
        }

        /// Verifies the ground tile is placed into the world layer after replacement.
        @Test
        @DisplayName("places the given ground tile into the level's world layer")
        void replacesWorldLayerWithGroundTile() {
            final EditorLevelDTO tile = EditorLevelDTO.create(POS_A, GROUND_GID);
            final UpdateWorldLayerDTO dto = new UpdateWorldLayerDTO(List.of(tile));
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(testLevel));
            Mockito.when(tileSetService.isGroundGID(GROUND_GID)).thenReturn(Boolean.TRUE);
            Mockito.when(levelRepository.save(testLevel)).thenReturn(testLevel);

            editorService.replaceWorldLayer(OWNER_ID, LEVEL_ID, dto);

            Assertions.assertNotNull(testLevel.getWorldLayer().get(POS_A));
        }

        /// Verifies that a tile at an out-of-bounds position is rejected before any save.
        @Test
        @DisplayName("throws IllegalArgumentException and aborts save when tile position is out of bounds")
        void throwsOnOutOfBoundsPosition() {
            final EditorLevelDTO tile = EditorLevelDTO.create(OUT_OF_BOUNDS_POS, GROUND_GID);
            final UpdateWorldLayerDTO dto = new UpdateWorldLayerDTO(List.of(tile));
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(testLevel));

            Assertions.assertThrows(IllegalArgumentException.class,
                    () -> editorService.replaceWorldLayer(OWNER_ID, LEVEL_ID, dto));

            Mockito.verify(levelRepository, Mockito.never()).save(Mockito.any());
        }

        /// Verifies that a tile with an invalid GID is rejected before any save.
        @Test
        @DisplayName("throws IllegalArgumentException and aborts save when ground GID is invalid")
        void throwsOnInvalidGroundGid() {
            final EditorLevelDTO tile = EditorLevelDTO.create(POS_A, INVALID_GID);
            final UpdateWorldLayerDTO dto = new UpdateWorldLayerDTO(List.of(tile));
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(testLevel));
            Mockito.when(tileSetService.isGroundGID(INVALID_GID)).thenReturn(Boolean.FALSE);

            Assertions.assertThrows(IllegalArgumentException.class,
                    () -> editorService.replaceWorldLayer(OWNER_ID, LEVEL_ID, dto));

            Mockito.verify(levelRepository, Mockito.never()).save(Mockito.any());
        }

        /// Verifies that a missing level aborts the operation before any save.
        @Test
        @DisplayName("throws LevelNotFoundException and aborts save when level is missing")
        void throwsLevelNotFoundAndAbortsSave() {
            final UpdateWorldLayerDTO dto = new UpdateWorldLayerDTO(List.of());
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.empty());

            Assertions.assertThrows(LevelNotFoundException.class,
                    () -> editorService.replaceWorldLayer(OWNER_ID, LEVEL_ID, dto));

            Mockito.verify(levelRepository, Mockito.never()).save(Mockito.any());
        }

        /// Verifies that a non-owner cannot replace the world layer.
        @Test
        @DisplayName("throws ForbiddenUserException and aborts save when caller is not the owner")
        void throwsForbiddenAndAbortsSave() {
            final UpdateWorldLayerDTO dto = new UpdateWorldLayerDTO(List.of());
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(testLevel));

            Assertions.assertThrows(ForbiddenUserException.class,
                    () -> editorService.replaceWorldLayer(OTHER_USER_ID, LEVEL_ID, dto));

            Mockito.verify(levelRepository, Mockito.never()).save(Mockito.any());
        }

        /// Verifies that a published level cannot have its world layer replaced.
        @Test
        @DisplayName("throws LevelPublishedException and aborts save when level is already published")
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
    @DisplayName("when replacing the object layer")
    class ReplaceObjectLayer {

        /// Verifies the level is saved
        /// and public publication state is reset on a successful empty-list replacement.
        @Test
        @DisplayName("saves the level and resets publication state with an empty object list")
        void savesEmptyLayerAndInvalidatesPublish() {
            final UpdateObjectLayerDTO dto = new UpdateObjectLayerDTO(List.of());
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(testLevel));
            Mockito.when(levelRepository.save(testLevel)).thenReturn(testLevel);

            final Level result = editorService.replaceObjectLayer(OWNER_ID, LEVEL_ID, dto);

            Assertions.assertSame(testLevel, result);
            Mockito.verify(levelRepository).save(testLevel);
            Mockito.verify(levelPublishService).resetLevelAfterEdit(testLevel, OWNER_ID);
        }

        /// Verifies that both DTOs are dispatched to the factory
        /// and the resulting objects land in the level's object layer.
        @Test
        @DisplayName("maps both DTOs through the factory, populates the object layer, and saves")
        void placesTwoObjectsViaFactoryAndSaves() {
            final EditorLevelDTO obj1 = EditorLevelDTO.create(POS_A, OBJECT_GID_A);
            final EditorLevelDTO obj2 = EditorLevelDTO.create(POS_B, OBJECT_GID_B);
            final UpdateObjectLayerDTO dto = new UpdateObjectLayerDTO(List.of(obj1, obj2));
            final Box realObjA = new Box(OBJECT_GID_A, POS_A, new Content.NoContent());
            final Box realObjB = new Box(OBJECT_GID_B, POS_B, new Content.NoContent());
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(testLevel));
            Mockito.when(tileSetService.isObjectGID(OBJECT_GID_A)).thenReturn(Boolean.TRUE);
            Mockito.when(tileSetService.isObjectGID(OBJECT_GID_B)).thenReturn(Boolean.TRUE);
            Mockito.when(gameObjectFactory.createGameObject(OBJECT_GID_A, POS_A, new Content.NoContent())).thenReturn(realObjA);
            Mockito.when(gameObjectFactory.createGameObject(OBJECT_GID_B, POS_B, new Content.NoContent())).thenReturn(realObjB);
            Mockito.when(levelRepository.save(testLevel)).thenReturn(testLevel);

            editorService.replaceObjectLayer(OWNER_ID, LEVEL_ID, dto);

            Assertions.assertSame(realObjA, Objects.requireNonNull(testLevel.getObjectLayer().get(POS_A)));
            Assertions.assertSame(realObjB, Objects.requireNonNull(testLevel.getObjectLayer().get(POS_B)));
            Mockito.verify(levelRepository).save(testLevel);
        }

        /// Verifies that two objects at the same position are rejected before any save.
        @Test
        @DisplayName("throws IllegalArgumentException and aborts save when two objects share a position")
        void throwsOnDuplicatePositionAndAbortsSave() {
            final EditorLevelDTO obj1 = EditorLevelDTO.create(POS_A, OBJECT_GID_A);
            final EditorLevelDTO obj2 = EditorLevelDTO.create(POS_A, OBJECT_GID_B);
            final UpdateObjectLayerDTO dto = new UpdateObjectLayerDTO(List.of(obj1, obj2));
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(testLevel));
            Mockito.when(tileSetService.isObjectGID(OBJECT_GID_A)).thenReturn(Boolean.TRUE);
            Mockito.when(tileSetService.isObjectGID(OBJECT_GID_B)).thenReturn(Boolean.TRUE);

            Assertions.assertThrows(IllegalArgumentException.class,
                    () -> editorService.replaceObjectLayer(OWNER_ID, LEVEL_ID, dto));

            Mockito.verify(levelRepository, Mockito.never()).save(Mockito.any());
        }

        /// Verifies that an object at an out-of-bounds position
        /// is rejected before any save.
        @Test
        @DisplayName("throws IllegalArgumentException and aborts save when object position is out of bounds")
        void throwsOnOutOfBoundsPosition() {
            final EditorLevelDTO obj = EditorLevelDTO.create(OUT_OF_BOUNDS_POS, OBJECT_GID_A);
            final UpdateObjectLayerDTO dto = new UpdateObjectLayerDTO(List.of(obj));
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(testLevel));

            Assertions.assertThrows(IllegalArgumentException.class,
                    () -> editorService.replaceObjectLayer(OWNER_ID, LEVEL_ID, dto));

            Mockito.verify(levelRepository, Mockito.never()).save(Mockito.any());
        }

        /// Verifies that an object with an invalid GID is rejected before any save.
        @Test
        @DisplayName("throws IllegalArgumentException and aborts save when object GID is invalid")
        void throwsOnInvalidObjectGid() {
            final EditorLevelDTO obj = EditorLevelDTO.create(POS_A, INVALID_GID);
            final UpdateObjectLayerDTO dto = new UpdateObjectLayerDTO(List.of(obj));
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(testLevel));
            Mockito.when(tileSetService.isObjectGID(INVALID_GID)).thenReturn(Boolean.FALSE);

            Assertions.assertThrows(IllegalArgumentException.class,
                    () -> editorService.replaceObjectLayer(OWNER_ID, LEVEL_ID, dto));

            Mockito.verify(levelRepository, Mockito.never()).save(Mockito.any());
        }

        /// Verifies that placing two start flags in the same layer
        /// is rejected before any save.
        @Test
        @DisplayName("throws IllegalArgumentException and aborts save when two start flags are placed")
        void throwsWhenTwoStartFlagsPlaced() {
            final EditorLevelDTO obj1 = EditorLevelDTO.create(POS_A, OBJECT_GID_A);
            final EditorLevelDTO obj2 = EditorLevelDTO.create(POS_B, OBJECT_GID_B);
            final UpdateObjectLayerDTO dto = new UpdateObjectLayerDTO(List.of(obj1, obj2));
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(testLevel));
            Mockito.when(tileSetService.isObjectGID(OBJECT_GID_A)).thenReturn(Boolean.TRUE);
            Mockito.when(tileSetService.isObjectGID(OBJECT_GID_B)).thenReturn(Boolean.TRUE);
            Mockito.when(gameObjectFactory.createGameObject(OBJECT_GID_A, POS_A, new Content.NoContent()))
                    .thenReturn(new StartFlag(OBJECT_GID_A, POS_A));
            Mockito.when(gameObjectFactory.createGameObject(OBJECT_GID_B, POS_B, new Content.NoContent()))
                    .thenReturn(new StartFlag(OBJECT_GID_B, POS_B));

            Assertions.assertThrows(IllegalArgumentException.class,
                    () -> editorService.replaceObjectLayer(OWNER_ID, LEVEL_ID, dto));

            Mockito.verify(levelRepository, Mockito.never()).save(Mockito.any());
        }

        /// Verifies that placing two exit doors in the same layer
        /// is rejected before any save.
        @Test
        @DisplayName("throws IllegalArgumentException and aborts save when two exit doors are placed")
        void throwsWhenTwoExitDoorsPlaced() {
            final EditorLevelDTO obj1 = EditorLevelDTO.create(POS_A, OBJECT_GID_A);
            final EditorLevelDTO obj2 = EditorLevelDTO.create(POS_B, OBJECT_GID_B);
            final UpdateObjectLayerDTO dto = new UpdateObjectLayerDTO(List.of(obj1, obj2));
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(testLevel));
            Mockito.when(tileSetService.isObjectGID(OBJECT_GID_A)).thenReturn(Boolean.TRUE);
            Mockito.when(tileSetService.isObjectGID(OBJECT_GID_B)).thenReturn(Boolean.TRUE);
            Mockito.when(gameObjectFactory.createGameObject(OBJECT_GID_A, POS_A, new Content.NoContent()))
                    .thenReturn(new ExitDoor(OBJECT_GID_A, POS_A));
            Mockito.when(gameObjectFactory.createGameObject(OBJECT_GID_B, POS_B, new Content.NoContent()))
                    .thenReturn(new ExitDoor(OBJECT_GID_B, POS_B));

            Assertions.assertThrows(IllegalArgumentException.class,
                    () -> editorService.replaceObjectLayer(OWNER_ID, LEVEL_ID, dto));

            Mockito.verify(levelRepository, Mockito.never()).save(Mockito.any());
        }

        /// Verifies that a missing level aborts the operation before any save.
        @Test
        @DisplayName("throws LevelNotFoundException and aborts save when level is missing")
        void throwsLevelNotFoundAndAbortsSave() {
            final UpdateObjectLayerDTO dto = new UpdateObjectLayerDTO(List.of());
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.empty());

            Assertions.assertThrows(LevelNotFoundException.class,
                    () -> editorService.replaceObjectLayer(OWNER_ID, LEVEL_ID, dto));

            Mockito.verify(levelRepository, Mockito.never()).save(Mockito.any());
        }

        /// Verifies that a non-owner cannot replace the object layer.
        @Test
        @DisplayName("throws ForbiddenUserException and aborts save when caller is not the owner")
        void throwsForbiddenAndAbortsSave() {
            final UpdateObjectLayerDTO dto = new UpdateObjectLayerDTO(List.of());
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(testLevel));

            Assertions.assertThrows(ForbiddenUserException.class,
                    () -> editorService.replaceObjectLayer(OTHER_USER_ID, LEVEL_ID, dto));

            Mockito.verify(levelRepository, Mockito.never()).save(Mockito.any());
        }

        /// Verifies that a published level cannot have its object layer replaced.
        @Test
        @DisplayName("throws LevelPublishedException and aborts save when level is already published")
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
    @DisplayName("when updating object properties")
    class UpdateObjectProperties {

        /// Verifies that the box content at the target position is updated
        /// and the level is saved with public publication state reset.
        @Test
        @DisplayName("updates the box content at the given position and saves the level")
        void updatesBoxContentAndSaves() {
            final Content newContent = new Content.SomeContent(CoinType.GOLD_COIN);
            testLevel.putObjectLayer(BOX_POS, new Box(BOX_GID, BOX_POS, new Content.NoContent()));
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(testLevel));
            Mockito.when(levelRepository.save(testLevel)).thenReturn(testLevel);

            editorService.updateObjectProperties(OWNER_ID, LEVEL_ID, new BoxPropertyUpdateDTO(BOX_POS, newContent));

            final Box updated = Assertions.assertInstanceOf(Box.class,
                    Objects.requireNonNull(testLevel.getObjectLayer().get(BOX_POS)));
            Assertions.assertEquals(newContent, updated.content());
            Mockito.verify(levelRepository).save(testLevel);
            Mockito.verify(levelPublishService).resetLevelAfterEdit(testLevel, OWNER_ID);
        }

        /// Verifies that a missing object at the target position throws before any save.
        @Test
        @DisplayName("throws NoSuchElementException and aborts save when no object exists at the position")
        void throwsWhenNoObjectAtPosition() {
            final BoxPropertyUpdateDTO dto = new BoxPropertyUpdateDTO(BOX_POS, new Content.NoContent());
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(testLevel));

            Assertions.assertThrows(NoSuchElementException.class,
                    () -> editorService.updateObjectProperties(OWNER_ID, LEVEL_ID, dto));

            Mockito.verify(levelRepository, Mockito.never()).save(Mockito.any());
        }

        /// Verifies that a non-box object at the target position throws before any save.
        @Test
        @DisplayName("throws IllegalArgumentException and aborts save when the object at the position is not a Box")
        void throwsWhenNonBoxAtPosition() {
            testLevel.putObjectLayer(BOX_POS, new StartFlag(BOX_GID, BOX_POS));
            final BoxPropertyUpdateDTO dto = new BoxPropertyUpdateDTO(BOX_POS, new Content.NoContent());
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(testLevel));

            Assertions.assertThrows(IllegalArgumentException.class,
                    () -> editorService.updateObjectProperties(OWNER_ID, LEVEL_ID, dto));

            Mockito.verify(levelRepository, Mockito.never()).save(Mockito.any());
        }

        /// Verifies that an out-of-bounds position throws before any save.
        @Test
        @DisplayName("throws IllegalArgumentException and aborts save when the target position is out of bounds")
        void throwsOnOutOfBoundsPosition() {
            final BoxPropertyUpdateDTO dto = new BoxPropertyUpdateDTO(OUT_OF_BOUNDS_POS, new Content.NoContent());
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(testLevel));

            Assertions.assertThrows(IllegalArgumentException.class,
                    () -> editorService.updateObjectProperties(OWNER_ID, LEVEL_ID, dto));

            Mockito.verify(levelRepository, Mockito.never()).save(Mockito.any());
        }

        /// Verifies that a missing level aborts the operation before any save.
        @Test
        @DisplayName("throws LevelNotFoundException and aborts save when level is missing")
        void throwsLevelNotFoundAndAbortsSave() {
            final BoxPropertyUpdateDTO dto = new BoxPropertyUpdateDTO(BOX_POS, new Content.NoContent());
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.empty());

            Assertions.assertThrows(LevelNotFoundException.class,
                    () -> editorService.updateObjectProperties(OWNER_ID, LEVEL_ID, dto));

            Mockito.verify(levelRepository, Mockito.never()).save(Mockito.any());
        }

        /// Verifies that a non-owner cannot update object properties.
        @Test
        @DisplayName("throws ForbiddenUserException and aborts save when caller is not the owner")
        void throwsForbiddenAndAbortsSave() {
            final BoxPropertyUpdateDTO dto = new BoxPropertyUpdateDTO(BOX_POS, new Content.NoContent());
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(testLevel));

            Assertions.assertThrows(ForbiddenUserException.class,
                    () -> editorService.updateObjectProperties(OTHER_USER_ID, LEVEL_ID, dto));

            Mockito.verify(levelRepository, Mockito.never()).save(Mockito.any());
        }

        /// Verifies that a published level cannot have its object properties updated.
        @Test
        @DisplayName("throws LevelPublishedException and aborts save when level is already published")
        void throwsPublishedAndAbortsSave() {
            final BoxPropertyUpdateDTO dto = new BoxPropertyUpdateDTO(BOX_POS, new Content.NoContent());
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(publishedLevel));

            Assertions.assertThrows(LevelPublishedException.class,
                    () -> editorService.updateObjectProperties(OWNER_ID, LEVEL_ID, dto));

            Mockito.verify(levelRepository, Mockito.never()).save(Mockito.any());
        }
    }
}
