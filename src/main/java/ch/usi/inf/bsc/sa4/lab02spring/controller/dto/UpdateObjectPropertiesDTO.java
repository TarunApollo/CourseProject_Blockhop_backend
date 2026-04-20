package ch.usi.inf.bsc.sa4.lab02spring.controller.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import ch.usi.inf.bsc.sa4.lab02spring.model.Position;

// By using polymorphism with (@JsonTypeInfo), the API accepts
// exactly what it needs for the specific object type being updated.
// eg: A box update only contains box properties etc.
// https://medium.com/@random_developer/jsontypeinfo-and-jsonsubtypes-in-jackson-0b76538848a6
// Pretty neat.

/// Base contract for DTOs used to update properties of an object in a level.
///
/// The JSON payload is polymorphic and resolved through the `type` field so
/// each concrete object update can expose only the properties it needs.
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = BoxPropertyUpdateDTO.class, name = "box")
})
public sealed interface UpdateObjectPropertiesDTO
    permits BoxPropertyUpdateDTO {
    /// Returns the position of the object being updated.
    /// @return the object's position in the level grid
    Position position();
}
