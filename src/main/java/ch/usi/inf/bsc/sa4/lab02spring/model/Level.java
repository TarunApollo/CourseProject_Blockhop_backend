package ch.usi.inf.bsc.sa4.lab02spring.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
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
    private boolean published;
    private int width;
    private int height;
    private ClearCondition clearCondition;
    private final Map<String, Date> timesPlayed = new HashMap<>();
    private HashMap<Position, GameObject> objectLayer = new HashMap<Position, GameObject>();
    private HashMap<Position, GroundObject> worldLayer = new HashMap<Position, GroundObject>();

    //TODO: make fields such as clearCondition private and add them to the constructor
    public Level(String title, String description, String creatorId) {
        this.title = title;
        this.description = description;
        this.published = false;
        this.creatorId = creatorId;
        this.width = 256;
        this.height = 14;
        this.clearCondition = new ClearCondition(ConditionType.NONE, 0);
    }

    @PersistenceCreator
    public Level(String creatorId, String title, String description, boolean published, int width, int height, ClearCondition clearCondition, Map<String, Date> timesPlayed, Map<Position, GameObject> objectLayer, Map<Position, GroundObject>  worldLayer) {
        this.creatorId = creatorId;
        this.title = title;
        this.description = description;
        this.published = published;
        this.width = width;
        this.height = height;
        this.clearCondition = clearCondition;
        this.objectLayer.putAll(objectLayer);
        this.worldLayer.putAll(worldLayer);
        this.timesPlayed.putAll(timesPlayed);
    }

    public Level cloneFor(String newCreatorId) {
        Level clone = new Level(this.title, this.description, newCreatorId);
        clone.clearCondition = this.clearCondition;
        clone.objectLayer = new HashMap<Position, GameObject>(this.objectLayer);
        clone.worldLayer = new HashMap<Position, GroundObject>(this.worldLayer);
        return clone;
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

    public String getCreatorId() {
        return creatorId;
    }

    public ClearCondition getClearCondition() {
        return clearCondition;
    }

    public Map<Position, GameObject> getObjectLayer() {
        return Map.copyOf(this.objectLayer);
    }

    public Map<Position, GroundObject> getWorldLayer() {
        return Map.copyOf(this.worldLayer);
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public void setClearCondition(ClearCondition clearCondition) {
        this.clearCondition = clearCondition;
    }
}
