package ch.usi.inf.bsc.sa4.lab02spring.controller.dto;

import java.util.List;

/// DTO for batch operations on the world (ground-tile) layer.
public record UpdateWorldLayerDTO(List<EditorLevelDTO> tiles) implements ILayerUpdateDTO {
    @Override
    public List<EditorLevelDTO> entries() {
        return tiles();
    }
}
