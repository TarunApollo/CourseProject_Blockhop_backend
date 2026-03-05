package ch.usi.inf.bsc.sa4.lab02spring.model;

import java.util.HashMap;

public class Level {
    String title;
    String description;
    boolean published;
    boolean exitDoorOpen;
    ClearCondition clearCondition;
    HashMap<Position,GameObject> objectLayer;
    HashMap<Position,GroundObject> worldLayer;
}
