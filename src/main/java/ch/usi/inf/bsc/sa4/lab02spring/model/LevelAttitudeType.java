package ch.usi.inf.bsc.sa4.lab02spring.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/// Enumeration of supported level-attitude values.
///
/// Each constant maps to the lower-case string used by the API.
public enum LevelAttitudeType {
    LIKE("like"),
    DISLIKE("dislike");

    /// External string representation used for JSON serialization.
    private final String value;

    LevelAttitudeType(final String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return value;
    }

    /// Converts a serialized string value back to the corresponding attitude type.
    ///
    /// @param value the external string representation
    /// @return the matching attitude type
    /// @throws IllegalArgumentException if the value is not recognized
    @JsonCreator
    public static LevelAttitudeType fromValue(final String value) {
        for (final LevelAttitudeType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown level attitude type: " + value);
    }
}