package ch.usi.inf.bsc.sa4.lab02spring.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ContentType {
    GOLD_COIN("Item_Coin_Gold", 100),
    SILVER_COIN("Item_Coin_Silver", 25),
    BRONZE_COIN("Item_Coin_Bronze", 5);

    private final String value;
    private final int coinValue;

    ContentType(String value, int coinValue) {
        this.value = value;
        this.coinValue = coinValue;
    }

    @JsonValue
    public String value() {
        return value;
    }

    public int coinValue() {
        return coinValue;
    }

    @JsonCreator
    public static ContentType fromValue(String value) {
        for (ContentType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown content type: " + value);
    }
}
