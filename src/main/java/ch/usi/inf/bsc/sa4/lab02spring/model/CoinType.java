package ch.usi.inf.bsc.sa4.lab02spring.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/// Enumeration of supported coin types.
///
/// Each constant maps to the external string value used in JSON payloads.
@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
public enum CoinType {
    GOLD_COIN("Item_Coin_Gold"),
    SILVER_COIN("Item_Coin_Silver"),
    BRONZE_COIN("Item_Coin_Bronze");

    /// External string representation used for JSON serialization.
    private final String value;

    CoinType(final String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return value;
    }

    /// Converts a serialized string value back to the corresponding coin type.
    /// @param value the external string representation
    /// @return the matching coin type
    /// @throws IllegalArgumentException if the value is not recognized
    @JsonCreator
    public static CoinType fromValue(final String value) {
        for (final CoinType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown content type: " + value);
    }
}
