package ch.usi.inf.bsc.sa4.lab02spring.controller.dto;

import ch.usi.inf.bsc.sa4.lab02spring.model.Position;

public record UpdateObjectPropertyDTO(Position position , String contentTypeString) {
}
