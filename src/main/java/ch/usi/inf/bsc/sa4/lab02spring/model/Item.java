package ch.usi.inf.bsc.sa4.lab02spring.model;

public abstract class Item extends GameObject {

    public Item() { super(); }

    public Item(Item item) {
        super(item);
    }
}
