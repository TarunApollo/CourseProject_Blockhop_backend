package ch.usi.inf.bsc.sa4.lab02spring.model;

public record Box(int gid, Position pos, BoxContentType content) implements Item {
}
