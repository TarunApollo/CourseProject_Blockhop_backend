package ch.usi.inf.bsc.sa4.lab02spring.model;

public sealed interface Condition {
    record NoClearCondition() implements Condition {}
    record SomeClearCondition(ClearConditionType target) implements Condition {}
}

