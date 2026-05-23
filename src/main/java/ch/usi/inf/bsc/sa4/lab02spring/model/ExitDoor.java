package ch.usi.inf.bsc.sa4.lab02spring.model;

import org.springframework.data.annotation.TypeAlias;

@TypeAlias("exit_door")
public record ExitDoor(String tileId, Position pos) implements Item {
}
