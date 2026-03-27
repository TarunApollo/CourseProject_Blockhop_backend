package ch.usi.inf.bsc.sa4.lab02spring.controller.dto;

import ch.usi.inf.bsc.sa4.lab02spring.model.Content;
import ch.usi.inf.bsc.sa4.lab02spring.model.Position;

public record BoxPropertyUpdateDTO(Position position, Content content) 
    implements UpdateObjectPropertiesDTO {}
