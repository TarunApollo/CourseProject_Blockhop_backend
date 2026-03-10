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
    private final int width = 256;
    private final int height = 14;
    private ClearCondition clearCondition = new ClearCondition(ConditionType.NONE, 0);
    private final Map<String, Date> timesPlayed = new HashMap<>();
    private final HashMap<Position, GameObject> objectLayer = new HashMap<>();
    private final HashMap<Position, GroundObject> worldLayer = new HashMap<>();

    public Level(String title, String description, String creatorId) {
        this.title = title;
        this.description = description;
        this.published = false;
        this.creatorId = creatorId;
    }

    @PersistenceCreator
    public Level(String creatorId, String title, String description, boolean published, int width, int height, ClearCondition clearCondition, Map<String, Date> timesPlayed, Map<Position, GameObject> objectLayer, Map<Position, GroundObject>  worldLayer) {
        this.creatorId = creatorId;
        this.title = title;
        this.description = description;
        this.published = published;
        this.clearCondition = clearCondition;
        this.objectLayer.putAll(objectLayer);
        this.worldLayer.putAll(worldLayer);
        this.timesPlayed.putAll(timesPlayed);
    }

    private Level(String title, String description, String creatorId, HashMap<Position, GameObject> objectLayer, HashMap<Position, GroundObject> worldLayer) {
        this(title, description, creatorId);
        this.objectLayer.putAll(objectLayer);
        this.worldLayer.putAll(worldLayer);
    }

    public Level cloneFor(String newCreatorId) {
        return new Level(this.title, this.description, newCreatorId, this.objectLayer, this.worldLayer);
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

    public Map<String, Date> getTimesPlayed() {
        return Map.copyOf(this.timesPlayed);
    }

    public void putObjectLayer(Position pos, GameObject gameObject) {
        this.objectLayer.put(pos, gameObject);
    }

    public void putWorldLayer(Position pos, GroundObject groundObject) {
        this.worldLayer.put(pos, groundObject);
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
