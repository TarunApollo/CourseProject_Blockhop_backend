package ch.usi.inf.bsc.sa4.lab02spring.model;

public class Box extends Item {
    final Item content;
    public Box(Item content) {
        super();
        // TODO: restrain this to Coin (frontend) only for batch 1
        this.content = content;
    }

    public Box(Box b) {
        super(b);
        this.content = (Item) b.content.copy();
    }

    @Override
    public Box copy() {
        return new Box(this);
    }
}


