package ch.usi.inf.bsc.sa4.lab02spring.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/// Sealed interface for box content.
/// JSON examples:
/// - Empty: {"type": "none"}
/// - Gold coin: {"type": "some", "coinType": "Item_Coin_Gold"}
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = Content.NoContent.class, name = "none"),
    @JsonSubTypes.Type(value = Content.SomeContent.class, name = "some")
})
public sealed interface Content permits Content.NoContent, Content.SomeContent {
    
    record NoContent() implements Content {}
    
    record SomeContent(CoinType coinType) implements Content {}
}
