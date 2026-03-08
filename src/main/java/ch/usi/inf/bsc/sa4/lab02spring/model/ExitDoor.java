package ch.usi.inf.bsc.sa4.lab02spring.model;

public class ExitDoor {
    boolean isOpen;

    //
    // No-arg constructor required by Spring Data MongoDB for deserialization.
    //
    public ExitDoor() {}

    public ExitDoor(ExitDoor other) {
        this.isOpen = other.isOpen;
    }
}
