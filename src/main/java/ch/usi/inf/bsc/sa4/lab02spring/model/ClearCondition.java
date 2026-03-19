package ch.usi.inf.bsc.sa4.lab02spring.model;

public record ClearCondition(Condition condition, int targetAmount) {
    public ClearCondition {
        if (targetAmount < 0) {
            throw new IllegalArgumentException("targetAmount must be non-negative");
        }
    }
    
}
