package ch.usi.inf.bsc.sa4.lab02spring.model;

public abstract class Enemy extends GameObject {

    public Enemy() {
        super();
    }

    public Enemy(Enemy e) {
        super(e);
    }
}
