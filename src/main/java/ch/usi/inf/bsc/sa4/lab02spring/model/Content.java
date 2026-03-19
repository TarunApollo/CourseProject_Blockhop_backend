package ch.usi.inf.bsc.sa4.lab02spring.model;

public sealed interface Content {
    record NoContent() implements Content {}
    record SomeContent(ContentType type) implements Content {}
}
