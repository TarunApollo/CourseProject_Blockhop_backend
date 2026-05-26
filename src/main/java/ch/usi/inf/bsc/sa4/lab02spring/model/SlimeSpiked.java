package ch.usi.inf.bsc.sa4.lab02spring.model;

import org.springframework.data.annotation.TypeAlias;

@TypeAlias("slime_spiked")
public record SlimeSpiked(String tileId, Position pos) implements Enemy {
}