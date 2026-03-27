package ch.usi.inf.bsc.sa4.lab02spring.controller.dto;

import java.util.List;

/// For batch operations on object layer.
public record UpdateObjectLayerDTO(
    List<EditorLevelDTO> objects
) {}
