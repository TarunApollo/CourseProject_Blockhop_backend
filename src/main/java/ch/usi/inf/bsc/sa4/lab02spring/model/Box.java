package ch.usi.inf.bsc.sa4.lab02spring.model;

import org.springframework.data.annotation.TypeAlias;

@TypeAlias("box")
public record Box(int gid, Position pos, Item content) implements Item {

    public boolean canBeContained() {
        return false;
    }

    public Box {
        if (!this.pos().equals(content.pos())) {
            throw new IllegalArgumentException("The Item's position inside this Box must be equal");
        }
        if (!content.canBeContained()) {
            throw new IllegalArgumentException("The Item can't be contained inside this Box");
        }
    }
}
