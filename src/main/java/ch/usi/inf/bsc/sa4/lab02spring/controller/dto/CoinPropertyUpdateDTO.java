package ch.usi.inf.bsc.sa4.lab02spring.controller.dto;

import ch.usi.inf.bsc.sa4.lab02spring.model.CoinType;
import ch.usi.inf.bsc.sa4.lab02spring.model.Position;

public record CoinPropertyUpdateDTO(Position position, CoinType coinType) 
    implements UpdateObjectPropertiesDTO {}
