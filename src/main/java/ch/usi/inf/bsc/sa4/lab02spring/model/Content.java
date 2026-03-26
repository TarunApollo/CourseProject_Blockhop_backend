package ch.usi.inf.bsc.sa4.lab02spring.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/// Sealed interface for box content.
/// JSON examples:
/// - Empty: {}
/// - Gold coin: {"type": "Item_Coin_Gold"}
public sealed interface Content {
    
    record NoContent() implements Content {}
    
    record SomeContent(CoinType type) implements Content {}

    @JsonCreator
    static Content fromJson(@JsonProperty("type") String type) {
        if (type == null || type.isEmpty()) {
            return new NoContent();
        }
        return new SomeContent(CoinType.fromValue(type));
    }
}
