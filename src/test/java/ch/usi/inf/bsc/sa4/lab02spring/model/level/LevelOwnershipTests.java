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
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.utils.ForbiddenUserException;

/// Tests covering [Level] ownership checks.
@DisplayName("In the Level ownership API")
@SuppressWarnings({ "PMD.TooManyStaticImports", "java:S2187" })
class LevelOwnershipTests {

    /// Tests for ownership methods.
    @Nested
    @DisplayName("methods isOwnedBy and ensureOwnedBy")
    class OwnershipMethods {

        /// The level instance.
        private Level level;

        /// Sets up ownership tests.
        @BeforeEach
        void setUp() {
            this.level = LevelTestFixtures.createLevelFor(LevelTestFixtures.createOwnerUser());
        }

        /// Verifies ownership checks for user IDs and user instances.
        @Test
        @DisplayName("isOwnedBy matches the owner for both user ids and user instances")
        void matchesOwnerForIdsAndUsers() {
            assertAll(
                    () -> assertTrue(this.level.isOwnedBy(LevelTestFixtures.OWNER_ID)),
                    () -> assertFalse(this.level.isOwnedBy(LevelTestFixtures.OTHER_ID)),
                    () -> assertTrue(this.level.isOwnedBy(new User(LevelTestFixtures.OWNER_ID, "Mario clone"))),
                    () -> assertFalse(this.level.isOwnedBy(LevelTestFixtures.createOtherUser())));
        }

        /// Tests for ensureOwnedBy.
        @Nested
        @DisplayName("method ensureOwnedBy")
        class EnsureOwnedByMethod {

            /// Verify ensureOwnedBy throws.
            @Test
            @DisplayName("it throws ForbiddenUserException for a non-owner")
            void throwsForbiddenUserException() {
                final Executable codeToExecute = () -> OwnershipMethods.this.level
                        .ensureOwnedBy(LevelTestFixtures.OTHER_ID);
                assertThrows(ForbiddenUserException.class, codeToExecute);
            }

            /// Verify ensureOwnedBy allowed for owner.
            @Test
            @DisplayName("it allows the owner")
            void allowsOwner() {
                final Executable codeToExecute = () -> OwnershipMethods.this.level
                        .ensureOwnedBy(LevelTestFixtures.OWNER_ID);
                assertDoesNotThrow(codeToExecute);
            }
        }
    }
}
