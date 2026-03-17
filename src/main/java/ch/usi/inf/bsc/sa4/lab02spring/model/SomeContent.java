package ch.usi.inf.bsc.sa4.lab02spring.model;

public record SomeContent(Item content) implements Content {
    public SomeContent {
        if (!content.canBeContained()){
            throw new IllegalArgumentException();
        }
    }
}
