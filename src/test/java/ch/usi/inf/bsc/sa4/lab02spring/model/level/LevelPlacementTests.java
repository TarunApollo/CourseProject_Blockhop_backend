package ch.usi.inf.bsc.sa4.lab02spring.model.level;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import ch.usi.inf.bsc.sa4.lab02spring.model.Box;
import ch.usi.inf.bsc.sa4.lab02spring.model.CoinType;
import ch.usi.inf.bsc.sa4.lab02spring.model.Content;
import ch.usi.inf.bsc.sa4.lab02spring.model.GroundObject;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.Position;
import ch.usi.inf.bsc.sa4.lab02spring.utils.ObjectPlacementConflictException;

/**
 * Tests covering placement rules and box-content updates for {@link Level}.
 */
@DisplayName("In the Level placement and box-content API")
@SuppressWarnings({ "PMD.TooManyStaticImports", "java:S2187" })
class LevelPlacementTests {

    /**
     * Tests for ensureObjectCanBePlacedAt.
     */
    @Nested
    @DisplayName("method ensureObjectCanBePlacedAt")
    class EnsureObjectCanBePlacedAtMethod {

        /** The level instance. */
        private Level level;
        /** A position for testing. */
        private Position position;

        /** Sets up placement tests. */
        @BeforeEach
        void setUp() {
            this.level = LevelTestFixtures.createTestLevel();
            this.position = new Position(3, 3);
        }

        /**
         * Tests for occupied positions.
         */
        @Nested
        @DisplayName("when the position is occupied")
        class WhenPositionIsOccupied {

            /** Verify world layer occupancy. */
            @Test
            @DisplayName("it throws when the world layer has an object at the position")
            void worldLayerOccupied() {
                EnsureObjectCanBePlacedAtMethod.this.level.putWorldLayer(EnsureObjectCanBePlacedAtMethod.this.position,
                        new GroundObject(5));
                final Executable codeToExecute = () -> EnsureObjectCanBePlacedAtMethod.this.level
                        .ensureObjectCanBePlacedAt(EnsureObjectCanBePlacedAtMethod.this.position);
                assertThrows(ObjectPlacementConflictException.class, codeToExecute);
            }

            /** Verify object layer occupancy. */
            @Test
            @DisplayName("it throws when the object layer has an object at the position")
            void objectLayerOccupied() {
                EnsureObjectCanBePlacedAtMethod.this.level.putObjectLayer(EnsureObjectCanBePlacedAtMethod.this.position,
                        LevelTestFixtures.createStartFlag(EnsureObjectCanBePlacedAtMethod.this.position));
                final Executable codeToExecute = () -> EnsureObjectCanBePlacedAtMethod.this.level
                        .ensureObjectCanBePlacedAt(EnsureObjectCanBePlacedAtMethod.this.position);
                assertThrows(ObjectPlacementConflictException.class, codeToExecute);
            }
        }

        /** Verify empty position. */
        @Test
        @DisplayName("it allows placement when both layers are empty at the position")
        void bothLayersEmpty() {
            final Executable codeToExecute = () -> this.level.ensureObjectCanBePlacedAt(this.position);
            assertDoesNotThrow(codeToExecute);
        }
    }

    /**
     * Tests for updateBoxContent.
     */
    @Nested
    @DisplayName("method updateBoxContent")
    class UpdateBoxContentMethod {

        /** The level instance. */
        private Level level;
        /** Valid position. */
        private Position validPosition;
        /** Invalid position. */
        private Position outOfBoundsPosition;

        /** Sets up box content update tests. */
        @BeforeEach
        void setUp() {
            this.level = LevelTestFixtures.createTestLevel();
            this.validPosition = new Position(5, 5);
            this.outOfBoundsPosition = new Position(256, 14);
        }

        /**
         * Tests for invalid updates.
         */
        @Nested
        @DisplayName("when position or object is invalid")
        class WhenPositionOrObjectIsInvalid {

            /** Verify out of bounds. */
            @Test
            @DisplayName("it throws IllegalArgumentException when position is out of bounds")
            void outOfBounds() {
                final Content noContent = new Content.NoContent();
                final Executable codeToExecute = () -> UpdateBoxContentMethod.this.level
                        .updateBoxContent(UpdateBoxContentMethod.this.outOfBoundsPosition, noContent);
                assertThrows(IllegalArgumentException.class, codeToExecute);
            }

            /** Verify not a box. */
            @Test
            @DisplayName("it throws IllegalArgumentException when the object is not a box")
            void notABox() {
                UpdateBoxContentMethod.this.level.putObjectLayer(UpdateBoxContentMethod.this.validPosition,
                        LevelTestFixtures.createStartFlag(UpdateBoxContentMethod.this.validPosition));
                final Content noContent = new Content.NoContent();
                final Executable codeToExecute = () -> UpdateBoxContentMethod.this.level
                        .updateBoxContent(UpdateBoxContentMethod.this.validPosition, noContent);
                assertThrows(IllegalArgumentException.class, codeToExecute);
            }
        }

        /** Verify missing object. */
        @Test
        @DisplayName("it throws NoSuchElementException when no object exists at the position")
        void noObjectAtPosition() {
            final Content noContent = new Content.NoContent();
            final Executable codeToExecute = () -> this.level.updateBoxContent(this.validPosition, noContent);
            assertThrows(NoSuchElementException.class, codeToExecute);
        }

        /** Verify box instance. */
        @Test
        @DisplayName("updateBoxContent keeps the object as a Box")
        void updatedObjectIsBox() {
            final Content newContent = new Content.SomeContent(CoinType.GOLD_COIN);
            this.level.putObjectLayer(this.validPosition, new Box(42, this.validPosition, new Content.NoContent()));
            this.level.updateBoxContent(this.validPosition, newContent);
            assertInstanceOf(Box.class, this.level.getObjectLayer().get(this.validPosition));
        }

        /** Verify box content update. */
        @Test
        @DisplayName("updateBoxContent updates the box content")
        void updatedBoxHasNewContent() {
            final Content newContent = new Content.SomeContent(CoinType.GOLD_COIN);
            this.level.putObjectLayer(this.validPosition, new Box(42, this.validPosition, new Content.NoContent()));
            this.level.updateBoxContent(this.validPosition, newContent);
            final Box updatedBox = assertInstanceOf(Box.class, this.level.getObjectLayer().get(this.validPosition));
            assertEquals(newContent, updatedBox.content());
        }
    }
}
