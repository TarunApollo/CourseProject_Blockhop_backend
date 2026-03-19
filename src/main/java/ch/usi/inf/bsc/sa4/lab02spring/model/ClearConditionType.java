package ch.usi.inf.bsc.sa4.lab02spring.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ClearConditionType {
    BOX("box"),
    COIN("coin"),
    SLIME("slime"),
    SNAIL("snail");


    private final String value;

    ClearConditionType(String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return value;
    }

    @JsonCreator
    public static ClearConditionType fromValue(String value) {
        for (ClearConditionType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown clear condition type: " + value);

    }
}
