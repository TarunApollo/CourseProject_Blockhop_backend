package ch.usi.inf.bsc.sa4.lab02spring.model;

import org.springframework.data.annotation.TypeAlias;
import java.util.List;

@TypeAlias("box")
public record Box(int gid, Position pos, List<Item> content) implements Item {

    /// @return `false`, as a box cannot be placed inside another box
    @Override
    public boolean canBeContained() {
        return false;
    }

    /// Creates a new Box with the given id, position, and content.
    /// @param gid the unique game identifier of this box
    /// @param pos the position of this box in the level
    /// @param content the list of items contained in this box (at most one)
    /// @throws IllegalArgumentException if the content contains more than one item,
    ///         if any contained item's position does not match the box's position,
    ///         or if any contained item cannot be contained inside a box
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