package ch.usi.inf.bsc.sa4.lab02spring.model;

public class Snail extends Enemy {

    //
    // No-arg constructor required by Spring Data MongoDB for deserialization.
    //
    public Snail() {
        super();
    }

    public Snail(Snail s) {
        super(s);
    }

    public Snail copy() {
        return new Snail(this);
    }
}
