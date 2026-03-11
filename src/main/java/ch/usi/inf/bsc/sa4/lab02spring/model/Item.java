package ch.usi.inf.bsc.sa4.lab02spring.model;

import org.springframework.data.annotation.TypeAlias;

@TypeAlias("item")
public interface Item extends GameObject {
    default boolean canBeContained() {
        return this instanceof Coin;
    }
}
