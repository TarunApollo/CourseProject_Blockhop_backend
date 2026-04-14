package ch.usi.inf.bsc.sa4.lab02spring.model;

public sealed interface GameObject permits Enemy, Item {
    int gid();

    Position pos();
}
