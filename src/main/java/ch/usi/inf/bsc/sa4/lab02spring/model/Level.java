package ch.usi.inf.bsc.sa4.lab02spring.model;

import org.springframework.data.annotation.Id;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("NullAway.Init")
public class Level {
    @Id
    private String id;
    String title;
    String description;
    boolean exitDoorOpen;
    boolean published;
    ClearCondition clearCondition;
    StartFlag startingItem;
    ExitDoor door;
    Map<String, Date> timesPlayed = new HashMap<>();
    HashMap<Position, GameObject> objectLayer = new HashMap<>();
    HashMap<Position, GroundObject> worldLayer = new HashMap<>();

    public Level(String title, String description) {
        this.title = title;
        this.description = description;
        this.published = false;
        this.exitDoorOpen = false;
    }

    public String getId() {
        return id;
    }
    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}
