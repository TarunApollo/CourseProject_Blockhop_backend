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
    private String title;
    private String description;
    private boolean exitDoorOpen;
    private boolean published;
    private ClearCondition clearCondition;
    private StartFlag startingItem;
    private ExitDoor door;
    private Map<String, Date> timesPlayed = new HashMap<>();
    private HashMap<Position, GameObject> objectLayer = new HashMap<>();
    private HashMap<Position, GroundObject> worldLayer = new HashMap<>();

    public Level(String title, String description) {
        this.title = title;
        this.description = description;
        this.published = false;
        this.exitDoorOpen = false;
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
}
