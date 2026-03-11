package ch.usi.inf.bsc.sa4.lab02spring.model;

import org.springframework.data.annotation.TypeAlias;

@TypeAlias("exit_door")
public record ExitDoor(int gid, Position pos) implements Item {
    public boolean canBeContained() {
        return false;
    }
}
