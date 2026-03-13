package ch.usi.inf.bsc.sa4.lab02spring.model;

import java.util.List;

public record Box(int gid, Position pos, List<Item> content) implements Item {

    @Override
    public boolean canBeContained() {
        return false;
    }
    public Box {
        content = List.copyOf(content);
        if (content.size() > 1) { // batch 1
            throw new IllegalArgumentException("There can only be one item inside this box");
        }
        content.forEach((Item item) -> {
            if (!pos.equals(item.pos())) {
                throw new IllegalArgumentException("The Item's position inside this Box must be equal");
            }
            if (!item.canBeContained()) {
                throw new IllegalArgumentException("The Item can not be contained inside a box");
            }
        });
    }
}