package ch.usi.inf.bsc.sa4.lab02spring.model;

public class Decoration extends Item {

    public Decoration() {}

    @Override
    public Decoration copy() {
        return new Decoration();
    }
}
