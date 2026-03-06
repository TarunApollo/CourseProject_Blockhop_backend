package ch.usi.inf.bsc.sa4.lab02spring.model;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Level {
    String title;
    String description;
    Map<String, Date> timesPlayed;
    boolean exitDoorOpen;
    boolean published;
    ClearCondition clearCondition;
    StartFlag startingItem;
    ExitDoor door;
    HashMap<Position, GameObject> objectLayer;
    HashMap<Position, GroundObject> worldLayer;
}
