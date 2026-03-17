package ch.usi.inf.bsc.sa4.lab02spring.model;

public record SomeCondition(ConditionType target) implements Condition {
    public SomeCondition {
        if (target == null) {
            throw new IllegalArgumentException();
        }
    }
}
