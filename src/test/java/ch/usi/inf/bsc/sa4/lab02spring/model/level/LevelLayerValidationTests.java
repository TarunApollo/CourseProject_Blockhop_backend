package ch.usi.inf.bsc.sa4.lab02spring.model.level;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import ch.usi.inf.bsc.sa4.lab02spring.model.GameObject;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.Position;
import ch.usi.inf.bsc.sa4.lab02spring.utils.ForbiddenLevelActionException;

/// Tests covering validation rules for [Level] object layers.
@DisplayName("In the Level layer validation API")
@SuppressWarnings({ "PMD.TooManyStaticImports", "java:S2187" })
class LevelLayerValidationTests {

    /// Tests for ensureValidObjectLayer.
    @Nested
    @DisplayName("method ensureValidObjectLayer")
    class EnsureValidObjectLayerMethod {

        /// The level instance.
        private Level level;

        /// Sets up validation tests.
        @BeforeEach
        void setUp() {
            this.level = LevelTestFixtures.createTestLevel();
        }

        /// Tests for invalid object layers.
        @Nested
        @DisplayName("when the object layer is invalid")
        class WhenObjectLayerIsInvalid {

            /// Verify multiple flags.
            @Test
            @DisplayName("it throws when there is more than one start flag")
            void moreThanOneFlag() {
                final Position pos1 = new Position(1, 1);
                final Position pos2 = new Position(3, 1);
                final Map<Position, GameObject> layer = Map.of(
                        pos1, LevelTestFixtures.createStartFlag(pos1),
                        pos2, LevelTestFixtures.createStartFlag(pos2));
                final Executable codeToExecute = () -> EnsureValidObjectLayerMethod.this.level
                        .ensureValidObjectLayer(layer);
                assertThrows(IllegalArgumentException.class, codeToExecute);
            }

            /// Verify multiple doors.
            @Test
            @DisplayName("it throws when there is more than one exit door")
            void moreThanOneDoor() {
                final Position pos1 = new Position(1, 1);
                final Position pos2 = new Position(3, 1);
                final Map<Position, GameObject> layer = Map.of(
                        pos1, LevelTestFixtures.createExitDoor(pos1),
                        pos2, LevelTestFixtures.createExitDoor(pos2));
                final Executable codeToExecute = () -> EnsureValidObjectLayerMethod.this.level
                        .ensureValidObjectLayer(layer);
                assertThrows(IllegalArgumentException.class, codeToExecute);
            }
        }

        /// Tests for valid object layers.
        @Nested
        @DisplayName("when the object layer is valid")
        class WhenObjectLayerIsValid {

            /// Verify empty layer.
            @Test
            @DisplayName("it allows an empty layer")
            void emptyLayer() {
                final Map<Position, GameObject> layer = Map.of();
                final Executable codeToExecute = () -> EnsureValidObjectLayerMethod.this.level
                        .ensureValidObjectLayer(layer);
                assertDoesNotThrow(codeToExecute);
            }

            /// Verify valid layer.
            @Test
            @DisplayName("it allows one flag and one door")
            void oneFlagOneDoor() {
                final Position flagPos = new Position(1, 1);
                final Position doorPos = new Position(2, 1);
                final Map<Position, GameObject> layer = Map.of(
                        flagPos, LevelTestFixtures.createStartFlag(flagPos),
                        doorPos, LevelTestFixtures.createExitDoor(doorPos));
                final Executable codeToExecute = () -> EnsureValidObjectLayerMethod.this.level
                        .ensureValidObjectLayer(layer);
                assertDoesNotThrow(codeToExecute);
            }
        }
    }

    /// Tests for ensurePublishableObjectLayer.
    @Nested
    @DisplayName("method ensurePublishableObjectLayer")
    class EnsurePublishableObjectLayerMethod {

        /// The level instance.
        private Level level;

        /// Sets up publishability validation tests.
        @BeforeEach
        void setUp() {
            this.level = LevelTestFixtures.createTestLevel();
        }

        /// Tests for non-publishable layers.
        @Nested
        @DisplayName("when the object layer is not publishable")
        class WhenObjectLayerIsNotPublishable {

            /// Verify missing flag.
            @Test
            @DisplayName("it throws when there are no start flags")
            void noFlag() {
                final Position doorPos = new Position(2, 1);
                EnsurePublishableObjectLayerMethod.this.level.putObjectLayer(
                        doorPos,
                        LevelTestFixtures.createExitDoor(doorPos));
                final Executable codeToExecute = () -> EnsurePublishableObjectLayerMethod.this.level
                        .ensurePublishableObjectLayer();
                assertThrows(ForbiddenLevelActionException.class, codeToExecute);
            }

            /// Verify missing door.
            @Test
            @DisplayName("it throws when there are no exit doors")
            void noDoor() {
                final Position flagPos = new Position(1, 1);
                EnsurePublishableObjectLayerMethod.this.level.putObjectLayer(
                        flagPos,
                        LevelTestFixtures.createStartFlag(flagPos));
                final Executable codeToExecute = () -> EnsurePublishableObjectLayerMethod.this.level
                        .ensurePublishableObjectLayer();
                assertThrows(ForbiddenLevelActionException.class, codeToExecute);
            }

            /// Verify multiple flags.
            @Test
            @DisplayName("it throws when there is more than one start flag")
            void moreThanOneFlag() {
                final Position flagPos1 = new Position(1, 1);
                final Position flagPos2 = new Position(3, 1);
                final Position doorPos = new Position(2, 1);
                EnsurePublishableObjectLayerMethod.this.level.putObjectLayer(
                        flagPos1,
                        LevelTestFixtures.createStartFlag(flagPos1));
                EnsurePublishableObjectLayerMethod.this.level.putObjectLayer(
                        flagPos2,
                        LevelTestFixtures.createStartFlag(flagPos2));
                EnsurePublishableObjectLayerMethod.this.level.putObjectLayer(
                        doorPos,
                        LevelTestFixtures.createExitDoor(doorPos));
                final Executable codeToExecute = () -> EnsurePublishableObjectLayerMethod.this.level
                        .ensurePublishableObjectLayer();
                assertThrows(ForbiddenLevelActionException.class, codeToExecute);
            }

            /// Verify multiple doors.
            @Test
            @DisplayName("it throws when there is more than one exit door")
            void moreThanOneDoor() {
                final Position flagPos = new Position(1, 1);
                final Position doorPos1 = new Position(2, 1);
                final Position doorPos2 = new Position(4, 1);
                EnsurePublishableObjectLayerMethod.this.level.putObjectLayer(
                        flagPos,
                        LevelTestFixtures.createStartFlag(flagPos));
                EnsurePublishableObjectLayerMethod.this.level.putObjectLayer(
                        doorPos1,
                        LevelTestFixtures.createExitDoor(doorPos1));
                EnsurePublishableObjectLayerMethod.this.level.putObjectLayer(
                        doorPos2,
                        LevelTestFixtures.createExitDoor(doorPos2));
                final Executable codeToExecute = () -> EnsurePublishableObjectLayerMethod.this.level
                        .ensurePublishableObjectLayer();
                assertThrows(ForbiddenLevelActionException.class, codeToExecute);
            }
        }

        /// Tests for publishable layers.
        @Nested
        @DisplayName("when the object layer is publishable")
        class WhenObjectLayerIsPublishable {

            /// Verify valid layer.
            @Test
            @DisplayName("it allows exactly one flag and one door")
            void oneFlagOneDoor() {
                final Position flagPos = new Position(1, 1);
                final Position doorPos = new Position(2, 1);
                EnsurePublishableObjectLayerMethod.this.level.putObjectLayer(
                        flagPos,
                        LevelTestFixtures.createStartFlag(flagPos));
                EnsurePublishableObjectLayerMethod.this.level.putObjectLayer(
                        doorPos,
                        LevelTestFixtures.createExitDoor(doorPos));
                final Executable codeToExecute = () -> EnsurePublishableObjectLayerMethod.this.level
                        .ensurePublishableObjectLayer();
                assertDoesNotThrow(codeToExecute);
            }
        }
    }
}
