package ch.usi.inf.bsc.sa4.lab02spring.model;

import org.springframework.data.annotation.TypeAlias;

@TypeAlias("Slime")
public record Slime(int gid, Position pos) implements Enemy {
}
