package ch.usi.inf.bsc.sa4.lab02spring.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum BoxContentType {
    EMPTY,
    BRONZE_COIN,
    SILVER_COIN,
    GOLD_COIN;

    @JsonValue
    public String toValue() {
        return name().toLowerCase();
    }

    @JsonCreator
    public static BoxContentType fromValue(String value) {
        return value == null || value.isEmpty() 
            ? EMPTY 
            : valueOf(value.toUpperCase());
    }

    public Content toContent() {
        return switch (this) {
            case BRONZE_COIN -> new Content.SomeContent(ContentType.BRONZE_COIN);
            case SILVER_COIN -> new Content.SomeContent(ContentType.SILVER_COIN);
            case GOLD_COIN -> new Content.SomeContent(ContentType.GOLD_COIN);
            case EMPTY -> new Content.NoContent();
        };
    }

    public ContentType toContentType() {
        return switch (this) {
            case BRONZE_COIN -> ContentType.BRONZE_COIN;
            case SILVER_COIN -> ContentType.SILVER_COIN;
            case GOLD_COIN -> ContentType.GOLD_COIN;
            case EMPTY -> throw new IllegalStateException("EMPTY has no content type");
        };
    }

    public int coinValue() {
        return this == EMPTY ? 0 : toContentType().coinValue();
    }
}
