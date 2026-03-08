package ch.usi.inf.bsc.sa4.lab02spring.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("NullAway.Init")
@Document(collection = "levels")
public class Level {
    @Id
    private String id;
    private final String creatorId;
    private String title;
    private String description;
    boolean exitDoorOpen;
    boolean published;
    private int width;
    private int height;
    ClearCondition clearCondition;
    StartFlag startingItem;
    ExitDoor door;
    Map<String, Date> timesPlayed = new HashMap<>();
    HashMap<Position, GameObject> objectLayer = new HashMap<>();
    HashMap<Position, GroundObject> worldLayer = new HashMap<>();

    //TODO: make fields such as clearCondition private and add them to the constructor
    public Level(String title, String description, String creatorId) {
        this.title = title;
        this.description = description;
        this.published = false;
        this.exitDoorOpen = false;
        this.creatorId = creatorId;
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

    public String getCreatorId() {
        return creatorId;
    }

    public boolean isPublished() {
        return published;
    }

    public Level cloneFor(String newCreatorId) {
        Level clone = new Level(this.title, this.description, newCreatorId);
        clone.exitDoorOpen = this.exitDoorOpen;
        clone.clearCondition = this.clearCondition;
        clone.startingItem = this.startingItem;
        clone.door = this.door;
        clone.objectLayer = new HashMap<>(this.objectLayer);
        clone.worldLayer = new HashMap<>(this.worldLayer);
        return clone;
    }
}
