package ch.usi.inf.bsc.sa4.lab02spring.model;

public class Snail extends Enemy {

    public Snail(Snail s) {
        super(s);
    }

    public Snail copy() {
        return new Snail(this);
    }
}
