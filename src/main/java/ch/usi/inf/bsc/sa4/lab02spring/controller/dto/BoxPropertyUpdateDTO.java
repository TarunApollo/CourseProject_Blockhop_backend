package ch.usi.inf.bsc.sa4.lab02spring.controller.dto;

import ch.usi.inf.bsc.sa4.lab02spring.model.Content;
import ch.usi.inf.bsc.sa4.lab02spring.model.Position;

// TODO: Add compact constructor to validate non-null position and content
public record BoxPropertyUpdateDTO(Position position, Content content) 
    implements UpdateObjectPropertiesDTO {}
