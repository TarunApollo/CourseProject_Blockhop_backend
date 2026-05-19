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
    private final String valueString;

    LevelAttitudeType(final String value) {
        this.valueString = value;
    }

    /// Returns the external string representation of this attitude type for
    /// JSON serialization.
    @JsonValue
    public String value() {
        return valueString;
    }

    /// Converts a serialized string value back to the corresponding attitude type.
    ///
    /// @param value the external string representation
    /// @return the matching attitude type
    /// @throws IllegalArgumentException if the value is not recognized
    @JsonCreator
    public static LevelAttitudeType fromValue(final String value) {
        for (final LevelAttitudeType type : values()) {
            if (type.valueString.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown level attitude type: " + value);
    }
}