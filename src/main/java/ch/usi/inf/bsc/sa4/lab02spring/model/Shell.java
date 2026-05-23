package ch.usi.inf.bsc.sa4.lab02spring.model;

import org.springframework.data.annotation.TypeAlias;

@TypeAlias("shell")
public record Shell(String tileId, Position pos) implements Item {
}
