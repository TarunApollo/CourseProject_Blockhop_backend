package ch.usi.inf.bsc.sa4.lab02spring.controller.dto;
import java.util.Optional;
import ch.usi.inf.bsc.sa4.lab02spring.model.ClearCondition;

public record UpdateLevelDTO(Optional<String> title, Optional<String> description, Optional<ClearCondition> clearCondition) {

}
