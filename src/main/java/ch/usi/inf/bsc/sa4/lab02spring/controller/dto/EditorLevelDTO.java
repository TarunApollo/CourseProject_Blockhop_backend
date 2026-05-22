package ch.usi.inf.bsc.sa4.lab02spring.controller.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import ch.usi.inf.bsc.sa4.lab02spring.model.Content;
import ch.usi.inf.bsc.sa4.lab02spring.model.Position;

public record EditorLevelDTO(Position position, String tileId, Content content) {
    /// Creates an editor-level DTO from JSON input.
    /// 
    /// @param position the position of the tile or object
    /// @param tileId   the catalog tile id
    /// @param content  the optional content associated with the object
    /// @return a new EditorLevelDTO
    @JsonCreator
    public static EditorLevelDTO create(
            @JsonProperty("position") final Position position,
            @JsonProperty("tileId") final String tileId,
            @JsonProperty("content") final Content content) {
        return new EditorLevelDTO(position, tileId,
                content != null ? content : new Content.NoContent());
    }

    /// Creates an editor-level DTO without content.
    /// 
    /// @param position the position of the tile or object
    /// @param tileId   the catalog tile id
    /// @return a new EditorLevelDTO with empty content
    public static EditorLevelDTO create(final Position position, final String tileId) {
        return new EditorLevelDTO(position, tileId, new Content.NoContent());
    }

}
