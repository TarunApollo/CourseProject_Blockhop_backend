package ch.usi.inf.bsc.sa4.lab02spring.model.level;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.Position;

/// Tests covering [Level] bounds validation.
@DisplayName("In the Level bounds API")
@SuppressWarnings({ "PMD.TooManyStaticImports", "java:S2187" })
class LevelBoundsTests {

    /// Tests for bounds methods.
    @Nested
    @DisplayName("methods isWithinBounds and ensureWithinBounds")
    class BoundsMethods {

        /// The level instance.
        private Level level;

        /// Sets up bounds tests.
        @BeforeEach
        void setUp() {
            this.level = LevelTestFixtures.createTestLevel();
        }

        /// Sets up tests for all boundary checks.
        @Test
        @DisplayName("isWithinBounds accepts in-range positions and rejects out-of-range positions")
        void checksBoundsAtRepresentativePositions() {
            assertAll(
                    () -> assertTrue(this.level.isWithinBounds(new Position(0, 0))),
                    () -> assertTrue(this.level.isWithinBounds(new Position(255, 13))),
                    () -> assertFalse(this.level.isWithinBounds(new Position(-1, 0))),
                    () -> assertFalse(this.level.isWithinBounds(new Position(256, 0))),
                    () -> assertFalse(this.level.isWithinBounds(new Position(0, -1))),
                    () -> assertFalse(this.level.isWithinBounds(new Position(0, 14))));
        }

        /// Tests for ensureWithinBounds.
        @Nested
        @DisplayName("method ensureWithinBounds")
        class EnsureWithinBoundsMethod {

            /// Verify null position.
            @Test
            @DisplayName("it throws IllegalArgumentException when position is null")
            @SuppressWarnings("NullAway")
            void nullPosition() {
                assertThrows(IllegalArgumentException.class, () -> BoundsMethods.this.level.ensureWithinBounds(null));
            }

            /// Verify out of bounds position.
            @Test
            @DisplayName("it throws IllegalArgumentException when position is out of bounds")
            void outOfBoundsPosition() {
                final Position pos = new Position(256, 14);
                final Executable codeToExecute = () -> BoundsMethods.this.level.ensureWithinBounds(pos);
                assertThrows(IllegalArgumentException.class, codeToExecute);
            }

            /// Verify valid position.
            @Test
            @DisplayName("it allows a valid position")
            void validPosition() {
                assertDoesNotThrow(() -> BoundsMethods.this.level.ensureWithinBounds(new Position(255, 13)));
            }
        }
    }
}
