package ch.usi.inf.bsc.sa4.lab02spring.model;

import org.springframework.data.annotation.TypeAlias;

@TypeAlias("coin")
public record Coin(int gid, Position pos, int value) implements Item {
    public Coin withGidAndValue(int newGid, int newValue) {
        return new Coin(newGid, this.pos(), newValue);
    }
}
