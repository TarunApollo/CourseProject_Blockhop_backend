package ch.usi.inf.bsc.sa4.lab02spring.controller.dto;

import java.util.List;

/// DTO for batch operations on the world (ground-tile) layer.
public record UpdateWorldLayerDTO(List<EditorLevelDTO> tiles) {
    public UpdateWorldLayerDTO {
        tiles = List.copyOf(tiles);
    }
}
