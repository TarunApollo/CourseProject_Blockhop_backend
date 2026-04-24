package ch.usi.inf.bsc.sa4.lab02spring.model;

import java.util.function.IntPredicate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

/**
 * Tests for TileObjectId: canonical constructor,
 * remove() factory, isRemoval(), and validated constructor.
 */
@DisplayName(" In the TileObjectId class ")
/* package */ class TileObjectIdTests {

    /** A positive GID value accepted by the validator. */
    private static final int VALID_GID = 5;

    /** A negative GID value rejected by the validator. */
    private static final int INVALID_GID = -1;

    /** Validator that accepts only positive integers. */
    private static final IntPredicate VALIDATOR = value -> value > 0;

    /**
     * Verifies that a TileObjectId with value zero
     * is recognised as a removal marker.
     */
    @DisplayName(" zero value should be recognized as removal ")
    @Test
    /* package */ void zeroValueIsRemoval() {
        final TileObjectId tileId = new TileObjectId(0);
        Assertions.assertTrue(tileId.isRemoval());
    }

    /**
     * Tests for the canonical single-argument constructor.
     */
    @DisplayName(" when using the canonical constructor ")
    @Nested
    /* package */ class CanonicalConstructor {

        /**
         * Verifies that the canonical constructor stores
         * the supplied value.
         */
        @DisplayName(" should store the given value ")
        @Test
        /* package */ void storesGivenValue() {
            final TileObjectId tileId = new TileObjectId(VALID_GID);
            Assertions.assertEquals(VALID_GID, tileId.value());
        }
    }

    /**
     * Tests for the remove() factory method.
     */
    @DisplayName(" method remove ")
    @Nested
    /* package */ class RemoveFactory {

        /**
         * Verifies that remove() returns a TileObjectId
         * whose value is zero.
         */
        @DisplayName(" should have value zero ")
        @Test
        /* package */ void hasValueZero() {
            final TileObjectId removal = TileObjectId.remove();
            Assertions.assertEquals(0, removal.value());
        }
    }

    /**
     * Tests for the isRemoval() method.
     */
    @DisplayName(" method isRemoval ")
    @Nested
    /* package */ class IsRemovalMethod {

        /**
         * Verifies that isRemoval() returns true for a
         * TileObjectId created via remove().
         */
        @DisplayName(" should return true for zero value ")
        @Test
        /* package */ void returnsTrueForZeroValue() {
            final TileObjectId removal = TileObjectId.remove();
            Assertions.assertTrue(removal.isRemoval());
        }

        /**
         * Verifies that isRemoval() returns false for
         * a non-zero TileObjectId.
         */
        @DisplayName(" should return false for non-zero value ")
        @Test
        /* package */ void returnsFalseForNonZero() {
            final TileObjectId tileId = new TileObjectId(VALID_GID);
            Assertions.assertFalse(tileId.isRemoval());
        }
    }

    /**
     * Tests for the two-argument validated constructor.
     */
    @DisplayName(" when using the validated constructor ")
    @Nested
    /* package */ class ValidatedConstructor {

        /**
         * Verifies that a valid GID is accepted and stored.
         */
        @DisplayName(" should accept a valid GID ")
        @Test
        /* package */ void acceptsValidGid() {
            final TileObjectId tileId = new TileObjectId(VALID_GID, VALIDATOR);
            Assertions.assertEquals(VALID_GID, tileId.value());
        }

        /**
         * Verifies that zero bypasses validation
         * (reserved removal marker).
         */
        @DisplayName(" should allow zero without validation ")
        @Test
        /* package */ void allowsZeroWithoutValidation() {
            final TileObjectId tileId = new TileObjectId(0, VALIDATOR);
            Assertions.assertEquals(0, tileId.value());
        }

        /**
         * Verifies that an invalid GID throws
         * IllegalArgumentException.
         */
        @DisplayName(" should throw for an invalid GID ")
        @Test
        /* package */ void throwsForInvalidGid() {
            final Executable exec = ValidatedConstructor::buildInvalidTileId;
            Assertions.assertThrows(IllegalArgumentException.class, exec);
        }

        /**
         * Attempts to construct a TileObjectId with an invalid GID.
         *
         * @return never returns; throws IllegalArgumentException
         */
        private static TileObjectId buildInvalidTileId() {
            return new TileObjectId(INVALID_GID, VALIDATOR);
        }
    }
}
