package ch.usi.inf.bsc.sa4.lab02spring.controller.dto;

import ch.usi.inf.bsc.sa4.lab02spring.model.ClearCondition;

public record UpdateLevelDTO(String title, String description, ClearCondition clearCondition) {

}
