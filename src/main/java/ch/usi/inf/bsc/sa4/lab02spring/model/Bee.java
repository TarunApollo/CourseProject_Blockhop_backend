package ch.usi.inf.bsc.sa4.lab02spring.model;

import org.springframework.data.annotation.TypeAlias;

@TypeAlias("bee")
public record Bee(String tileId, Position pos) implements Enemy {
}
