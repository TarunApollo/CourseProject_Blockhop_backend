package ch.usi.inf.bsc.sa4.lab02spring.utils;

import java.util.function.IntPredicate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link TileObjectIdValidator}. */
@DisplayName("In the TileObjectIdValidator")
@SuppressWarnings("java:S2187")
/* package */ class TileObjectIdValidatorTests {

    /** A non-zero GID used by tests. */
    private static final int VALID_GID = 5;

    /** A non-zero GID rejected by the predicate-rejects-everything validator. */
    private static final int INVALID_GID = 7;

    /** Validator that accepts only VALID_GID. */
    private static final IntPredicate ACCEPTS_VALID = gid -> gid == VALID_GID;

    /** Validator that rejects every GID. */
    private static final IntPredicate REJECTS_ALL = gid -> false;

    /** Tests that value 0 always passes regardless of validator. */
    @Nested
    @DisplayName("when value is 0 (removal marker)")
    /* default */ class ZeroValue {

        @Test
        @DisplayName("does not throw even when validator rejects everything")
        void zeroBypassesValidator() {
            Assertions.assertDoesNotThrow(
                    () -> TileObjectIdValidator.validate(0, REJECTS_ALL));
        }
    }

    /** Tests that non-zero values are routed through the predicate. */
    @Nested
    @DisplayName("when value is non-zero")
    /* default */ class NonZeroValue {

        @Test
        @DisplayName("does not throw when the validator accepts the value")
        void acceptedValueDoesNotThrow() {
            Assertions.assertDoesNotThrow(
                    () -> TileObjectIdValidator.validate(VALID_GID, ACCEPTS_VALID));
        }

        @Test
        @DisplayName("throws IllegalArgumentException when the validator rejects the value")
        void rejectedValueThrows() {
            Assertions.assertThrows(IllegalArgumentException.class,
                    () -> TileObjectIdValidator.validate(INVALID_GID, ACCEPTS_VALID));
        }

        @Test
        @DisplayName("error message includes the rejected value")
        @SuppressWarnings("NullAway")
        void rejectedValueIncludedInMessage() {
            final IllegalArgumentException ex = Assertions.assertThrows(
                    IllegalArgumentException.class,
                    () -> TileObjectIdValidator.validate(INVALID_GID, ACCEPTS_VALID));
            Assertions.assertTrue(ex.getMessage().contains(String.valueOf(INVALID_GID)));
        }
    }
}
