package ch.usi.inf.bsc.sa4.lab02spring.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.mongodb.core.mapping.Document;

import ch.usi.inf.bsc.sa4.lab02spring.utils.ForbiddenUserException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.LevelPublishedException;

import java.util.Collections;
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
    private ClearCondition clearCondition = new ClearCondition(ConditionType.NONE, 0);
    private final HashMap<Position, GameObject> objectLayer = new HashMap<>();
    private final HashMap<Position, GroundObject> worldLayer = new HashMap<>();

    public Level(String title, String description, User user) {
        this.title = title;
        this.description = description;
        this.published = false;
        this.creator = user;
        this.clearCondition = new ClearCondition(ConditionType.NONE, 0);
    }

    @PersistenceCreator
    public Level(User creator, String title, String description, boolean published, ClearCondition clearCondition, Map<Position, GameObject> objectLayer, Map<Position, GroundObject>  worldLayer) {
        this.creator = creator;
        this.title = title;
        this.description = description;
        this.published = published;
        this.clearCondition = clearCondition;
        this.objectLayer.putAll(objectLayer);
        this.worldLayer.putAll(worldLayer);
    }

    private Level(String title, String description, User creator,  HashMap<Position, GameObject> objectLayer, HashMap<Position, GroundObject> worldLayer) {
        this(title, description, creator);
        this.objectLayer.putAll(objectLayer);
        this.worldLayer.putAll(worldLayer);
    }

    public Level cloneFor(User creator) { return new Level(this.title, this.description, creator, this.objectLayer, this.worldLayer); }

    public boolean isPublished() { return this.published; }

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

    public Map<Position, GameObject> getObjectLayer() {
        return Collections.unmodifiableMap(objectLayer);
    }

    public Map<Position, GroundObject> getWorldLayer() {
        return Collections.unmodifiableMap(worldLayer);
    }

    public void putObjectLayer(Position pos, GameObject gameObject) {
        this.objectLayer.put(pos, gameObject);
    }

    public void removeGroundObject(Position pos){
        this.worldLayer.remove(pos);
    }

    public void putWorldLayer(Position pos, GroundObject groundObject) {
        this.worldLayer.put(pos, groundObject);
    }

    public GroundObject removeWorldLayer(Position position) {
        return this.worldLayer.remove(position);
    }

    public int getWidth(){
        return width;
    }

    public int getHeight(){
        return height;
    }


    public void setTitle(String title) { this.title = title; }

    public void setDescription(String description) { this.description = description; }

    public void setPublished(boolean published) { this.published = published; }

    public void setClearCondition(ClearCondition clearCondition) { this.clearCondition = clearCondition; }


    /// =====
    /// Domain logic methods
    /// =====

     // previously this check was duplicated in EditorService and LevelService
     // with inconsistent implementations (some compared User object directly;
     // some compared IDs..)
    public boolean isOwnedBy(String userId) {
        return this.creator.getId().equals(userId);
    }

    public boolean isOwnedBy(User user) {
        return this.creator.getId().equals(user.getId());
    }

    public boolean canBeModified() {
        return !this.published;
    }

    public void ensureModifiable() {
        if (this.published) {
            throw new LevelPublishedException("Cannot modify a published level");
        }
    }

    public void ensureOwnedBy(String userId) {
        if (!isOwnedBy(userId)) {
            throw new ForbiddenUserException("Only the level owner can perform this action");
        }
    }

    public boolean isWithinBounds(Position position) {
        return position.x() >= 0 && position.x() < this.width
            && position.y() >= 0 && position.y() < this.height;
    }

    public void ensureWithinBounds(Position position) {
        if (position == null) {
            throw new IllegalArgumentException("Position cannot be null");
        }
        if (!isWithinBounds(position)) {
            throw new IllegalArgumentException(
                // LLM generated format string because I'm lazy
                String.format("Position (%d, %d) is out of bounds. Valid range: x=[0,%d], y=[0,%d]",
                    position.x(), position.y(), this.width - 1, this.height - 1)
            );
        }
    }
}
