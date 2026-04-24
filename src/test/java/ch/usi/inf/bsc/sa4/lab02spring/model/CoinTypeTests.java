package ch.usi.inf.bsc.sa4.lab02spring.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

/**
 * Tests for CoinType enum: value() JSON key
 * and fromValue() deserialization.
 */
/* package */ class CoinTypeTests {

    /** JSON value for the gold coin variant. */
    private static final String GOLD_VALUE = "Item_Coin_Gold";

    /** JSON value for the silver coin variant. */
    private static final String SILVER_VALUE = "Item_Coin_Silver";

    /** JSON value for the bronze coin variant. */
    private static final String BRONZE_VALUE = "Item_Coin_Bronze";

    /** A string that does not map to any CoinType constant. */
    private static final String UNKNOWN_VALUE = "Item_Coin_Unknown";

    /**
     * Verifies that fromValue returns the correct constant
     * for the gold coin JSON key.
     */
    @Test
    /* package */ void fromValueReturnsGoldCoin() {
        Assertions.assertEquals(CoinType.GOLD_COIN, CoinType.fromValue(GOLD_VALUE));
    }

    /**
     * Tests for the value() method.
     */
    @Nested
    /* package */ class ValueMethod {

        /**
         * Verifies that GOLD_COIN returns the correct JSON key.
         */
        @Test
        /* package */ void goldCoinReturnsCorrectValue() {
            Assertions.assertEquals(GOLD_VALUE, CoinType.GOLD_COIN.value());
        }

        /**
         * Verifies that SILVER_COIN returns the correct JSON key.
         */
        @Test
        /* package */ void silverCoinReturnsCorrectValue() {
            Assertions.assertEquals(SILVER_VALUE, CoinType.SILVER_COIN.value());
        }

        /**
         * Verifies that BRONZE_COIN returns the correct JSON key.
         */
        @Test
        /* package */ void bronzeCoinReturnsCorrectValue() {
            Assertions.assertEquals(BRONZE_VALUE, CoinType.BRONZE_COIN.value());
        }
    }

    /**
     * Tests for the fromValue() factory method.
     */
    @Nested
    /* package */ class FromValueMethod {

        /**
         * Verifies that fromValue returns GOLD_COIN
         * for the gold JSON key.
         */
        @Test
        /* package */ void returnsGoldCoin() {
            Assertions.assertEquals(CoinType.GOLD_COIN, CoinType.fromValue(GOLD_VALUE));
        }

        /**
         * Verifies that fromValue returns SILVER_COIN
         * for the silver JSON key.
         */
        @Test
        /* package */ void returnsSilverCoin() {
            Assertions.assertEquals(
                    CoinType.SILVER_COIN, CoinType.fromValue(SILVER_VALUE));
        }

        /**
         * Verifies that fromValue returns BRONZE_COIN
         * for the bronze JSON key.
         */
        @Test
        /* package */ void returnsBronzeCoin() {
            Assertions.assertEquals(
                    CoinType.BRONZE_COIN, CoinType.fromValue(BRONZE_VALUE));
        }

        /**
         * Verifies that fromValue throws
         * IllegalArgumentException for an unrecognised string.
         */
        @Test
        /* package */ void throwsForUnknownValue() {
            final Executable exec = () -> CoinType.fromValue(UNKNOWN_VALUE);
            Assertions.assertThrows(IllegalArgumentException.class, exec);
        }
    }
}
