package ch.usi.inf.bsc.sa4.lab02spring.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CoinType {
    GOLD_COIN("Item_Coin_Gold"),
    SILVER_COIN("Item_Coin_Silver"),
    BRONZE_COIN("Item_Coin_Bronze");

    private final String value;

    CoinType(String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return value;
    }

    @JsonCreator
    public static CoinType fromValue(String value) {
        for (CoinType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown content type: " + value);
    }
}
