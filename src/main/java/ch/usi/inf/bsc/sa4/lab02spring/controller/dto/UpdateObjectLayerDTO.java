package ch.usi.inf.bsc.sa4.lab02spring.controller.dto;

import java.util.List;

// TODO: Add compact constructor to validate non-null and non-empty objects list
public record UpdateObjectLayerDTO(List<EditorLevelDTO> objects) {}
