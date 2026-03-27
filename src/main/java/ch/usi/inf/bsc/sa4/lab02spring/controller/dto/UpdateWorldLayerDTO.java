package ch.usi.inf.bsc.sa4.lab02spring.controller.dto;
import java.util.List;

//TODO: docs
// for batch ops
public record UpdateWorldLayerDTO(
    List<EditorLevelDTO> tiles
) {}