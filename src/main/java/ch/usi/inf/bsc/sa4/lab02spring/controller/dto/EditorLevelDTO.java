package ch.usi.inf.bsc.sa4.lab02spring.controller.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.usi.inf.bsc.sa4.lab02spring.model.CreateObjectProperties;
import ch.usi.inf.bsc.sa4.lab02spring.model.Position;

public record EditorLevelDTO(Position position, int gid, CreateObjectPropertiesDTO properties) {
    
    @JsonCreator
    public static EditorLevelDTO create(
            @JsonProperty("position") Position position,
            @JsonProperty("gid") int gid,
            @JsonProperty("properties") CreateObjectPropertiesDTO properties) {
        return new EditorLevelDTO(position, gid, 
            properties != null ? properties : CreateObjectPropertiesDTO.EMPTY);
    }
    
    public CreateObjectProperties getProperties() {
        return properties.toDomain();
    }
}
