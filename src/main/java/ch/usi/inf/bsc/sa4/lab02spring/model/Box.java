package ch.usi.inf.bsc.sa4.lab02spring.model;

public class Box extends Item {
    final Item content;
    public Box(Item content) {
        // TODO: restrain this to Coin (frontend) only for batch 1
        this.content = content;
    }
}


