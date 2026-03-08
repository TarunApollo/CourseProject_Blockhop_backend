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
    private boolean exitDoorOpen;
    private boolean published;
    private final int width = 256;
    private final int height = 14;
    private ClearCondition clearCondition;
    private StartFlag startingItem;
    private ExitDoor door;
    private Map<String, Date> timesPlayed = new HashMap<>();
    private HashMap<Position, GameObject> objectLayer = new HashMap<>();
    private HashMap<Position, GroundObject> worldLayer = new HashMap<>();

    //TODO: make fields such as clearCondition private and add them to the constructor
    public Level(String title, String description, String creatorId) {
        this.title = title;
        this.description = description;
        this.published = false;
        this.exitDoorOpen = false;
        this.creatorId = creatorId;
    }
    public boolean isPublished(){
        return this.published;
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

    public void setTitle(String newTitle){
        this.title = newTitle;
    }
    public void setDescription(String newDescription){
        this.description = newDescription;
    }

    public void setClearCondition(ClearCondition newClearCondition) {
        this.clearCondition = newClearCondition;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public Level cloneFor(String newCreatorId) {
        Level clone = new Level(this.title, this.description, newCreatorId);
        clone.exitDoorOpen = this.exitDoorOpen;
        clone.clearCondition = this.clearCondition;
        clone.startingItem = this.startingItem;
        clone.door = this.door;
        clone.objectLayer = new HashMap<>();
        this.objectLayer.forEach((key, value) ->
                clone.objectLayer.put(key, value.copy()));
        clone.worldLayer = new HashMap<>();
        clone.worldLayer.putAll(this.worldLayer);
        return clone;
    }
}
