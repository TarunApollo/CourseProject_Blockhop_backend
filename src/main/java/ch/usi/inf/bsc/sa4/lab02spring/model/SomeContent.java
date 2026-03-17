package ch.usi.inf.bsc.sa4.lab02spring.model;

public record SomeContent(ContentType type) implements Content {
    public SomeContent {
        if (type == null){
            throw new IllegalArgumentException();
        }
    }
}
