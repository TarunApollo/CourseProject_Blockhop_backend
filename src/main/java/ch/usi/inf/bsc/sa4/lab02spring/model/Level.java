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
    private boolean published;
    private int width;
    private int height;
    private ClearCondition clearCondition;
    private final Map<String, Date> timesPlayed = new HashMap<>();
    private HashMap<Position, GameObject> objectLayer = new HashMap<>();
    private HashMap<Position, GroundObject> worldLayer = new HashMap<>();

    public Level(String title, String description, String creatorId) {
        this.title = title;
        this.description = description;
        this.published = false;
        this.creatorId = creatorId;
        this.width = 256;
        this.height = 14;
        this.clearCondition = new ClearCondition(ConditionType.NONE, 0);
    }

    public Level cloneFor(String newCreatorId) {
        Level clone = new Level(this.title, this.description, newCreatorId);
        clone.clearCondition = this.clearCondition;
        clone.objectLayer = new HashMap<>(this.objectLayer);
        clone.worldLayer = new HashMap<>(this.worldLayer);
        return clone;
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

    public ClearCondition getClearCondition() {
        return clearCondition;
    }

    public Map<String, Date> getTimesPlayed() {
        return timesPlayed;
    }

    public HashMap<Position, GameObject> getObjectLayer() {
        return objectLayer;
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
