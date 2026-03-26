package ch.usi.inf.bsc.sa4.lab02spring.controller.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import ch.usi.inf.bsc.sa4.lab02spring.model.Content;
import ch.usi.inf.bsc.sa4.lab02spring.model.Position;

// TODO: Add compact constructor to validate non-null position
public record EditorLevelDTO(Position position, int gid, Content content) {
    
    @JsonCreator
    public static EditorLevelDTO create(
            @JsonProperty("position") Position position,
            @JsonProperty("gid") int gid,
            @JsonProperty("content") Content content) {
        return new EditorLevelDTO(position, gid, 
            content != null ? content : new Content.NoContent());
    }
    
    //convenience overload
    public static EditorLevelDTO create(Position position, int gid) {
        return new EditorLevelDTO(position, gid, new Content.NoContent());
    }
}
