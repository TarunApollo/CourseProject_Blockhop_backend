package ch.usi.inf.bsc.sa4.lab02spring.model;

import org.springframework.data.annotation.TypeAlias;
import java.util.List;

@TypeAlias("box")
public record Box(int gid, Position pos, Content content) implements Item {

    @Override
    public boolean canBeContained() {
        return false;
    }
}