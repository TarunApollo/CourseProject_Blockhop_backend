package ch.usi.inf.bsc.sa4.lab02spring.model;

public class StartFlag extends Item {

    public StartFlag() {}

    //
    // Copy constructor for deepcopying a StartFlaf
    // Preserves inherited fields from the parent class
    //
    public StartFlag(StartFlag s) {
        super(s);
    }

    @Override
    public StartFlag copy() {
        return new StartFlag(this);
    }
}
