package ch.usi.inf.bsc.sa4.lab02spring.model;

/// Sealed interface for box content.
/// JSON examples:
/// - Empty: {}
/// - Gold coin: {"type": "Item_Coin_Gold"}
public sealed interface Content {
    record NoContent() implements Content {}
    record SomeContent(ContentType type) implements Content {}
}
