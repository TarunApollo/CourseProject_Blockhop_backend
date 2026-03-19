package ch.usi.inf.bsc.sa4.lab02spring.model;

import org.springframework.data.annotation.TypeAlias;


/// Creates a new Box with the given id, position, and content.
/// @param gid the unique game identifier of this box
/// @param pos the position of this box in the level
/// @param content a Content that can be a NoContent or SomeContent
///
@TypeAlias("box")
public record Box(int gid, Position pos, Content content) implements Item {
}