package ch.usi.inf.bsc.sa4.lab02spring.model;

import java.io.StringBufferInputStream;

public class Coin extends Item {
    private final int value; // 5, 25, 100

    public int getValue() {
        return value;
    }

    public Coin(int value) {
        this.value = value;
    }

    @Override
    public Coin copy() {
        return new Coin(this.value);
    }
}
