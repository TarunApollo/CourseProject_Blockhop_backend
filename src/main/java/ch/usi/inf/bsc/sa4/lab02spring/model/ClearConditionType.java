package ch.usi.inf.bsc.sa4.lab02spring.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/// Enumeration of supported clear-condition target types.
/// Each value is serialized to the lower-case string used by the API and can
/// be reconstructed from that string during deserialization.
@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
public enum ClearConditionType {
    BOX("box"),
    COIN("coin"),
    SLIME("slime_normal"),
    SNAIL("snail"),
    BEE("bee"),
    SLIME_SPIKED("slime_spiked");


    /// External string representation used for JSON serialization.
    private final String value;

    ClearConditionType(final String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return value;
    }

    /// Converts a serialized string value back to the corresponding enum constant.
    ///
    /// @param value the external string representation
    /// @return the matching clear-condition type
    /// @throws IllegalArgumentException if the value is not recognized
    @JsonCreator
    public static ClearConditionType fromValue(final String value) {
        for (final ClearConditionType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown clear condition type: " + value);

    }
}
