package ch.usi.inf.bsc.sa4.lab02spring.model;

import org.springframework.data.annotation.TypeAlias;

@TypeAlias("spiked_alien")
public record SpikedAlien(int gid, Position pos) implements Enemy {
}