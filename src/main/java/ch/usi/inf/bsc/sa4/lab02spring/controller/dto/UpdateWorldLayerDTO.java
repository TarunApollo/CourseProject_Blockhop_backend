package ch.usi.inf.bsc.sa4.lab02spring.controller.dto;

import ch.usi.inf.bsc.sa4.lab02spring.model.Position;

import java.util.List;
import java.util.Map;

//TODO: docs
// for batch ops
public record UpdateWorldLayerDTO(
    List<EditorLevelDTO> tiles
) {}