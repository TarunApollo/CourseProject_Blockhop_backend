package ch.usi.inf.bsc.sa4.lab02spring.controller.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import ch.usi.inf.bsc.sa4.lab02spring.model.Content;
import ch.usi.inf.bsc.sa4.lab02spring.model.Position;

public record EditorLevelDTO(Position position, int gid, Content content) {
    /**
     * Creates an editor-level DTO from JSON input.
     * <p>
     * If {@code content} is {@code null}, an empty content object is used.
     *
     * @param position the position of the tile or object
     * @param gid the global tile id
     * @param content the optional content associated with the object
     * @return a new {@code EditorLevelDTO}
     */
    @JsonCreator
    public static EditorLevelDTO create(
            @JsonProperty("position") Position position,
            @JsonProperty("gid") int gid,
            @JsonProperty("content") Content content) {
        return new EditorLevelDTO(position, gid, 
            content != null ? content : new Content.NoContent());
    }

    /**
     * Creates an editor-level DTO without content.
     *
     * @param position the position of the tile or object
     * @param gid the global tile id
     * @return a new {@code EditorLevelDTO} with empty content
     */
    public static EditorLevelDTO create(Position position, int gid) {
        return new EditorLevelDTO(position, gid, new Content.NoContent());
    }
}
