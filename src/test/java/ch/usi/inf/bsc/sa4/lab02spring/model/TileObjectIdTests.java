package ch.usi.inf.bsc.sa4.lab02spring.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.function.IntPredicate;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

/**
 * Tests for TileObjectId: canonical constructor,
 * remove() factory, isRemoval(), and validated constructor.
 */
/* package */ class TileObjectIdTests {

    /** A positive GID value accepted by the validator. */
    private static final int VALID_GID = 5;

    /** A negative GID value rejected by the validator. */
    private static final int INVALID_GID = -1;

    /** Validator that accepts only positive integers. */
    private static final IntPredicate VALIDATOR = value -> value > 0;

    /**
     * Instantiates a new TileObjectIdTests.
     */
    /* package */ TileObjectIdTests() {
        // Required by pmd:AtLeastOneConstructor
    }

    /**
     * Verifies that a TileObjectId with value zero
     * is recognised as a removal marker.
     */
    @Test
    /* package */ void zeroValueIsRemoval() {
        final TileObjectId tileId = new TileObjectId(0);
        assertTrue(tileId.isRemoval());
    }

    /**
     * Tests for the canonical single-argument constructor.
     */
    @Nested
    /* package */ class CanonicalConstructor {

        /**
         * Instantiates a new CanonicalConstructor.
         */
        /* package */ CanonicalConstructor() {
            // Required by pmd:AtLeastOneConstructor
        }

        /**
         * Verifies that the canonical constructor stores
         * the supplied value.
         */
        @Test
        /* package */ void storesGivenValue() {
            final TileObjectId tileId = new TileObjectId(VALID_GID);
            assertEquals(VALID_GID, tileId.value());
        }
    }

    /**
     * Tests for the remove() factory method.
     */
    @Nested
    /* package */ class RemoveFactory {

        /**
         * Instantiates a new RemoveFactory.
         */
        /* package */ RemoveFactory() {
            // Required by pmd:AtLeastOneConstructor
        }

        /**
         * Verifies that remove() returns a TileObjectId
         * whose value is zero.
         */
        @Test
        /* package */ void hasValueZero() {
            final TileObjectId removal = TileObjectId.remove();
            assertEquals(0, removal.value());
        }
    }

    /**
     * Tests for the isRemoval() method.
     */
    @Nested
    /* package */ class IsRemovalMethod {

        /**
         * Instantiates a new IsRemovalMethod.
         */
        /* package */ IsRemovalMethod() {
            // Required by pmd:AtLeastOneConstructor
        }

        /**
         * Verifies that isRemoval() returns true for a
         * TileObjectId created via remove().
         */
        @Test
        /* package */ void returnsTrueForZeroValue() {
            final TileObjectId removal = TileObjectId.remove();
            assertTrue(removal.isRemoval());
        }

        /**
         * Verifies that isRemoval() returns false for
         * a non-zero TileObjectId.
         */
        @Test
        /* package */ void returnsFalseForNonZero() {
            final TileObjectId tileId = new TileObjectId(VALID_GID);
            assertFalse(tileId.isRemoval());
        }
    }

    /**
     * Tests for the two-argument validated constructor.
     */
    @Nested
    /* package */ class ValidatedConstructor {

        /**
         * Instantiates a new ValidatedConstructor.
         */
        /* package */ ValidatedConstructor() {
            // Required by pmd:AtLeastOneConstructor
        }

        /**
         * Verifies that a valid GID is accepted and stored.
         */
        @Test
        /* package */ void acceptsValidGid() {
            final TileObjectId tileId = new TileObjectId(VALID_GID, VALIDATOR);
            assertEquals(VALID_GID, tileId.value());
        }

        /**
         * Verifies that zero bypasses validation
         * (reserved removal marker).
         */
        @Test
        /* package */ void allowsZeroWithoutValidation() {
            final TileObjectId tileId = new TileObjectId(0, VALIDATOR);
            assertEquals(0, tileId.value());
        }

        /**
         * Verifies that an invalid GID throws
         * IllegalArgumentException.
         */
        @Test
        /* package */ void throwsForInvalidGid() {
            final Executable exec =
                    () -> new TileObjectId(INVALID_GID, VALIDATOR);
            assertThrows(IllegalArgumentException.class, exec);
        }
    }
}
