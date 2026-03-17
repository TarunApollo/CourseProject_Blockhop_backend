package ch.usi.inf.bsc.sa4.lab02spring.model;

public record ConditionType(String value) {
    public ConditionType {
        if (value == null) {
            throw new IllegalArgumentException();
        }
    }
}
