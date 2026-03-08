package ch.usi.inf.bsc.sa4.lab02spring.model;

public class Coin extends Item {
    private int value; // 5, 25, 100

    public int getValue() {
        return value;
    }

    //
    // No-arg constructor required by Spring Data MongoDB for deserialization.
    //
    public Coin() {
        super();
    }

    public Coin(int value) {
        this.value = value;
    }

    @Override
    public Coin copy() {
        return new Coin(this.value);
    }
}
