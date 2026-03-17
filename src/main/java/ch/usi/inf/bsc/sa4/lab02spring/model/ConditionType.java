package ch.usi.inf.bsc.sa4.lab02spring.model;

public record ConditionType(String value) {
    public ConditionType {
        if (value == null) {
            throw new IllegalArgumentException();
        }
        if (!value.equals("box")
                && !value.equals("coin")
                && !value.equals("slime")
                && !value.equals("snail")) {
            throw new IllegalArgumentException();
        }
    }
}
