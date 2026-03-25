package ch.usi.inf.bsc.sa4.lab02spring.controller.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.usi.inf.bsc.sa4.lab02spring.model.BoxContentType;
import ch.usi.inf.bsc.sa4.lab02spring.model.CreateObjectProperties;

public record CreateObjectPropertiesDTO(
    BoxContentType boxContentType
) {
    
    public static final CreateObjectPropertiesDTO EMPTY = new CreateObjectPropertiesDTO(BoxContentType.EMPTY);
    
    @JsonCreator
    public static CreateObjectPropertiesDTO create(
            @JsonProperty("boxContentType") BoxContentType boxContentType) {
        return new CreateObjectPropertiesDTO(
            boxContentType != null ? boxContentType : BoxContentType.EMPTY
        );
    }
    
    public CreateObjectProperties toDomain() {
        return new CreateObjectProperties(boxContentType);
    }
}
