package ch.usi.inf.bsc.sa4.lab02spring.model;

public record SomeCondition(GameObject target) implements Condition{
    public SomeCondition {
        if (!target.canBeCondition()){
            throw new IllegalArgumentException();
        }
    }
}
