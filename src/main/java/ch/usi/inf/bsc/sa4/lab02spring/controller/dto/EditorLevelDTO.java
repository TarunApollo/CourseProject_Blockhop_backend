package ch.usi.inf.bsc.sa4.lab02spring.controller.dto;

import java.util.Optional;
import ch.usi.inf.bsc.sa4.lab02spring.model.Position;

//not using optional here as it is mandatory to have both of these things here, 
// as we cannot modify the worldlayer
//  without knowing where or what we need to change.

public record EditorLevelDTO(Position position , int gid ){
    
};