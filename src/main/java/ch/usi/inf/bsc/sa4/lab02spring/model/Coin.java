package ch.usi.inf.bsc.sa4.lab02spring.model;

import org.springframework.data.annotation.TypeAlias;

@TypeAlias("coin")
public record Coin(int gid, Position pos, int value) implements Item {
    public boolean canBeContained() {
        return true;
    }
}
