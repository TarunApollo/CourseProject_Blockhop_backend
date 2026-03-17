package ch.usi.inf.bsc.sa4.lab02spring.model;

import org.springframework.data.annotation.TypeAlias;

@TypeAlias("game_object")
public interface GameObject {
    int gid();

    Position pos();

    default boolean canBeCondition() {
        return switch (this) {
            case Box box -> true;
            case Slime slime -> true;
            case Snail snail -> true;
            case Coin value -> true;
            default -> false;
        };
    }
}
