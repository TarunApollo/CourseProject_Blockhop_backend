package ch.usi.inf.bsc.sa4.lab02spring.model;

import org.springframework.data.annotation.TypeAlias;

@TypeAlias("item")
public interface Item extends GameObject {
    default boolean canBeContained() {
        switch (this) {
            case Coin c: return true;
            default: return false;
        }
    }
}
