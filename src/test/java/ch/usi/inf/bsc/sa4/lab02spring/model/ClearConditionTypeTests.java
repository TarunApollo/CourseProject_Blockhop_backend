package ch.usi.inf.bsc.sa4.lab02spring.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

/// Tests for ClearConditionType enum: value() JSON key
/// and fromValue() deserialization.
@DisplayName(" In the ClearConditionType enum ")
/* package */ class ClearConditionTypeTests {

    /// JSON value for the BOX clear condition.
    private static final String BOX_VALUE = "box";

    /// JSON value for the COIN clear condition.
    private static final String COIN_VALUE = "coin";

    /// JSON value for the SLIME clear condition.
    private static final String SLIME_VALUE = "slime_normal";

    /// JSON value for the SLIME_SPIKED clear condition.
    private static final String SLIME_SPIKED_VALUE = "slime_spiked";

    /// JSON value for the SNAIL clear condition.
    private static final String SNAIL_VALUE = "snail";

    /// A string that does not map to any ClearConditionType.
    private static final String UNKNOWN_VALUE = "unknown";

    /// Verifies that fromValue returns the correct constant
    /// for the box JSON key.
    @DisplayName(" fromValue returns the correct constant for the box JSON key ")
    @Test
    /* package */ void fromValueReturnsBox() {
        Assertions.assertEquals(
                ClearConditionType.BOX,
                ClearConditionType.fromValue(BOX_VALUE));
    }

    /// Tests for the value() method.
    @DisplayName(" tests for the value() method ")
    @Nested
    /* package */ class ValueMethod {

        /// Verifies that BOX returns the correct JSON key.        
        @DisplayName(" verifies that BOX returns the correct JSON key ")
        @Test
        /* package */ void boxReturnsCorrectValue() {
            Assertions.assertEquals(BOX_VALUE, ClearConditionType.BOX.value());
        }

        
        /// Verifies that COIN returns the correct JSON key.
        @DisplayName(" verifies that COIN returns the correct JSON key ")
        @Test
        /* package */ void coinReturnsCorrectValue() {
            Assertions.assertEquals(COIN_VALUE, ClearConditionType.COIN.value());
        }

        
        /// Verifies that SLIME returns the correct JSON key.
        @DisplayName(" verifies that SLIME returns the correct JSON key ")
        @Test
        /* package */ void slimeReturnsCorrectValue() {
            Assertions.assertEquals(SLIME_VALUE, ClearConditionType.SLIME.value());
        }

        /// Verifies that SLIME_SPIKED returns the correct JSON key.
        @DisplayName(" verifies that SLIME_SPIKED returns the correct JSON key ")
        @Test
        /* package */ void slimeSpikedReturnsCorrectValue() {
            Assertions.assertEquals(SLIME_SPIKED_VALUE, ClearConditionType.SLIME_SPIKED.value());
        }

        /// Verifies that SNAIL returns the correct JSON key.
        @DisplayName(" verifies that SNAIL returns the correct JSON key ")
        @Test
        /* package */ void snailReturnsCorrectValue() {
            Assertions.assertEquals(SNAIL_VALUE, ClearConditionType.SNAIL.value());
        }
    }

    /// Tests for the fromValue() factory method.    
    @DisplayName(" tests for the fromValue() factory method ")
    @Nested
    /* package */ class FromValueMethod {

        /// Verifies that fromValue returns BOX
        /// for the box JSON key.
        @DisplayName(" verifies that fromValue returns BOX for the box JSON key ")
        @Test
        /* package */ void returnsBox() {
            Assertions.assertEquals(
                    ClearConditionType.BOX,
                    ClearConditionType.fromValue(BOX_VALUE));
        }

        /// Verifies that fromValue returns COIN
        /// for the coin JSON key.
        
        @DisplayName(" verifies that fromValue returns COIN for the coin JSON key ")
        @Test
        /* package */ void returnsCoin() {
            Assertions.assertEquals(
                    ClearConditionType.COIN,
                    ClearConditionType.fromValue(COIN_VALUE));
        }

        
        /// Verifies that fromValue returns SLIME
        /// for the slime JSON key.
        
        @DisplayName(" verifies that fromValue returns SLIME for the slime JSON key ")
        @Test
        /* package */ void returnsSlime() {
            Assertions.assertEquals(
                    ClearConditionType.SLIME,
                    ClearConditionType.fromValue(SLIME_VALUE));
        }

        /// Verifies that fromValue returns SLIME_SPIKED
        /// for the slime spiked JSON key.
        @DisplayName(" verifies that fromValue returns SLIME_SPIKED for the slime spiked JSON key ")
        @Test
        /* package */ void returnsSlimeSpiked() {
            Assertions.assertEquals(
                    ClearConditionType.SLIME_SPIKED,
                    ClearConditionType.fromValue(SLIME_SPIKED_VALUE));
        }

        
        /// Verifies that fromValue returns SNAIL
        /// for the snail JSON key.
        @DisplayName(" verifies that fromValue returns SNAIL for the snail JSON key ")
        @Test
        /* package */ void returnsSnail() {
            Assertions.assertEquals(
                    ClearConditionType.SNAIL,
                    ClearConditionType.fromValue(SNAIL_VALUE));
        }

        
        /// Verifies that fromValue throws
        /// IllegalArgumentException for an unknown string.
        @DisplayName(" verifies that fromValue throws IllegalArgumentException for an unknown string ")
        @Test
        /* package */ void throwsForUnknownValue() {
            final Executable exec = () -> ClearConditionType.fromValue(UNKNOWN_VALUE);
            Assertions.assertThrows(IllegalArgumentException.class, exec);
        }
    }
}
