package ch.usi.inf.bsc.sa4.lab02spring.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("NullAway.Init")
@Document(collection = "levels")
public class Level {
    @Id
    private String id;
    @DBRef
    private final User creator;
    private String title;
    private String description;
    private boolean published;
    private final int width = 256;
    private final int height = 14;
    private ClearCondition clearCondition;
    private final Map<String, Date> timesPlayed = new HashMap<>();
    private final HashMap<Position, GameObject> objectLayer = new HashMap<>();
    private final HashMap<Position, GroundObject> worldLayer = new HashMap<>();

    //TODO: make fields such as clearCondition private and add them to the constructor
    public Level(String title, String description, User user) {
        this.title = title;
        this.description = description;
        this.published = false;
        this.creator = user;
        this.clearCondition = new ClearCondition(ConditionType.NONE, 0);
    }

    private Level(String title, String description, User creator, HashMap<Position, GameObject> objectLayer, HashMap<Position, GroundObject> worldLayer) {
        this(title, description, creator);
        this.objectLayer.putAll(objectLayer);
        this.worldLayer.putAll(worldLayer);
    }

    public Level cloneFor(User creator) {
        return new Level(this.title, this.description, creator, this.objectLayer, this.worldLayer);
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

    public User getCreator() {
        return creator;
    }

    public ClearCondition getClearCondition() {
        return clearCondition;
    }

    public Map<String, Date> getTimesPlayed() {
        return timesPlayed;
    }

    public Map<Position, GameObject> getObjectLayer() {
        return Collections.unmodifiableMap(objectLayer);
    }

    public HashMap<Position, GroundObject> getWorldLayer() {
        return worldLayer;
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
