package ch.usi.inf.bsc.sa4.lab02spring.model;

import org.springframework.data.annotation.TypeAlias;


/// Creates a new Box with the given id, position, and content.
/// @param tileId the catalog tile identifier of this box
/// @param pos the position of this box in the level
/// @param content a Content that can be a NoContent or SomeContent
///
@TypeAlias("box")
public record Box(String tileId, Position pos, Content content) implements Item {
    /// Returns a new box with the same id and position, but with updated content.
    /// @param newContent the content to store in the new box
    /// @return a new box instance containing the provided content
    public Box withContent(final Content newContent) {
        return new Box(this.tileId(), this.pos(), newContent);
    }
}
