package ch.usi.inf.bsc.sa4.lab02spring.model;

import org.springframework.data.annotation.TypeAlias;

@TypeAlias("box")
public record Box(int gid, Position pos, Item content) implements Item {
    public Box {
        if (!this.pos().equals(content.pos())) {

        }
    }
}
