package ch.usi.inf.bsc.sa4.lab02spring.controller.dto;

import ch.usi.inf.bsc.sa4.lab02spring.model.BoxContentType;
import ch.usi.inf.bsc.sa4.lab02spring.model.Position;

public record BoxPropertyUpdateDTO(Position position, BoxContentType content) 
    implements UpdateObjectPropertiesDTO {}
