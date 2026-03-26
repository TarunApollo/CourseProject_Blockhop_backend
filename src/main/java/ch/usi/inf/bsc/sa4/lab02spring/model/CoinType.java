package ch.usi.inf.bsc.sa4.lab02spring.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CoinType {
    BRONZE("Item_Coin_Bronze", 129),
    SILVER("Item_Coin_Silver", 119),
    GOLD("Item_Coin_Gold", 109);

    private final String tileType;
    private final int gid;

    CoinType(String tileType, int gid) {
        this.tileType = tileType;
        this.gid = gid;
    }

    @JsonValue
    public String toValue() {
        return name().toLowerCase();
    }

    public String tileType() {
        return tileType;
    }

    public int gid() {
        return gid;
    }

    public int coinValue() {
        return switch (this) {
            case BRONZE -> ContentType.BRONZE_COIN.coinValue();
            case SILVER -> ContentType.SILVER_COIN.coinValue();
            case GOLD -> ContentType.GOLD_COIN.coinValue();
        };
    }

    @JsonCreator
    public static CoinType fromValue(String value) {
        return value == null || value.isEmpty() 
            ? BRONZE 
            : valueOf(value.toUpperCase());
    }
}
