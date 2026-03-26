package ch.usi.inf.bsc.sa4.lab02spring.controller.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import ch.usi.inf.bsc.sa4.lab02spring.model.Position;

// By using polymorphism with (@JsonTypeInfo), the API accepts
// exactly what it needs for the specific object type being updated.
// eg: A box update only contains box properties etc.
// https://medium.com/@random_developer/jsontypeinfo-and-jsonsubtypes-in-jackson-0b76538848a6
// Pretty neat.
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = BoxPropertyUpdateDTO.class, name = "box")
})
public sealed interface UpdateObjectPropertiesDTO 
    permits BoxPropertyUpdateDTO {
    
    Position position();
}
