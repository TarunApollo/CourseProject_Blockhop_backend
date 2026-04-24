package ch.usi.inf.bsc.sa4.lab02spring.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

/**
 * Tests for ClearConditionType enum: value() JSON key
 * and fromValue() deserialization.
 */
/* package */ class ClearConditionTypeTests {

    /** JSON value for the BOX clear condition. */
    private static final String BOX_VALUE = "box";

    /** JSON value for the COIN clear condition. */
    private static final String COIN_VALUE = "coin";

    /** JSON value for the SLIME clear condition. */
    private static final String SLIME_VALUE = "slime";

    /** JSON value for the SNAIL clear condition. */
    private static final String SNAIL_VALUE = "snail";

    /** A string that does not map to any ClearConditionType. */
    private static final String UNKNOWN_VALUE = "unknown";

    /**
     * Instantiates a new ClearConditionTypeTests.
     */
    /* package */ ClearConditionTypeTests() {
        // Required by pmd:AtLeastOneConstructor
    }

    /**
     * Verifies that fromValue returns the correct constant
     * for the box JSON key.
     */
    @Test
    /* package */ void fromValueReturnsBox() {
        assertEquals(
                ClearConditionType.BOX,
                ClearConditionType.fromValue(BOX_VALUE));
    }

    /**
     * Tests for the value() method.
     */
    @Nested
    /* package */ class ValueMethod {

        /**
         * Instantiates a new ValueMethod.
         */
        /* package */ ValueMethod() {
            // Required by pmd:AtLeastOneConstructor
        }

        /**
         * Verifies that BOX returns the correct JSON key.
         */
        @Test
        /* package */ void boxReturnsCorrectValue() {
            assertEquals(BOX_VALUE, ClearConditionType.BOX.value());
        }

        /**
         * Verifies that COIN returns the correct JSON key.
         */
        @Test
        /* package */ void coinReturnsCorrectValue() {
            assertEquals(COIN_VALUE, ClearConditionType.COIN.value());
        }

        /**
         * Verifies that SLIME returns the correct JSON key.
         */
        @Test
        /* package */ void slimeReturnsCorrectValue() {
            assertEquals(SLIME_VALUE, ClearConditionType.SLIME.value());
        }

        /**
         * Verifies that SNAIL returns the correct JSON key.
         */
        @Test
        /* package */ void snailReturnsCorrectValue() {
            assertEquals(SNAIL_VALUE, ClearConditionType.SNAIL.value());
        }
    }

    /**
     * Tests for the fromValue() factory method.
     */
    @Nested
    /* package */ class FromValueMethod {

        /**
         * Instantiates a new FromValueMethod.
         */
        /* package */ FromValueMethod() {
            // Required by pmd:AtLeastOneConstructor
        }

        /**
         * Verifies that fromValue returns BOX
         * for the box JSON key.
         */
        @Test
        /* package */ void returnsBox() {
            assertEquals(
                    ClearConditionType.BOX,
                    ClearConditionType.fromValue(BOX_VALUE));
        }

        /**
         * Verifies that fromValue returns COIN
         * for the coin JSON key.
         */
        @Test
        /* package */ void returnsCoin() {
            assertEquals(
                    ClearConditionType.COIN,
                    ClearConditionType.fromValue(COIN_VALUE));
        }

        /**
         * Verifies that fromValue returns SLIME
         * for the slime JSON key.
         */
        @Test
        /* package */ void returnsSlime() {
            assertEquals(
                    ClearConditionType.SLIME,
                    ClearConditionType.fromValue(SLIME_VALUE));
        }

        /**
         * Verifies that fromValue returns SNAIL
         * for the snail JSON key.
         */
        @Test
        /* package */ void returnsSnail() {
            assertEquals(
                    ClearConditionType.SNAIL,
                    ClearConditionType.fromValue(SNAIL_VALUE));
        }

        /**
         * Verifies that fromValue throws
         * IllegalArgumentException for an unknown string.
         */
        @Test
        /* package */ void throwsForUnknownValue() {
            final Executable exec =
                    () -> ClearConditionType.fromValue(UNKNOWN_VALUE);
            assertThrows(IllegalArgumentException.class, exec);
        }
    }
}
