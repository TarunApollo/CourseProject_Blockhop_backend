package ch.usi.inf.bsc.sa4.lab02spring.model;

public class Slime extends Enemy {

    public Slime() {
        super();
    }

    @Override
    public Slime copy() {
        return new Slime();
    }
}