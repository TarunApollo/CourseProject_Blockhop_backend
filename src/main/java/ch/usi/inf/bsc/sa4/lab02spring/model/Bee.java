package ch.usi.inf.bsc.sa4.lab02spring.model;

import org.springframework.data.annotation.TypeAlias;

@TypeAlias("bee")
public record Bee(int gid, Position pos) implements Enemy {}
