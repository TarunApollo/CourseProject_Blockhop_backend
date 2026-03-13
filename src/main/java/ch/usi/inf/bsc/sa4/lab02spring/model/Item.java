package ch.usi.inf.bsc.sa4.lab02spring.model;

public sealed interface Item extends GameObject permits Box, Coin, Decoration, StartFlag, ExitDoor{
    default boolean canBeContained() {
        return true;
    }
}
