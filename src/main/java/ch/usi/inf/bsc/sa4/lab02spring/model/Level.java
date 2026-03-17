package ch.usi.inf.bsc.sa4.lab02spring.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.mongodb.core.mapping.Document;
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
    private ClearCondition clearCondition;
    private final HashMap<Position, GameObject> objectLayer = new HashMap<>();
    private final HashMap<Position, GroundObject> worldLayer = new HashMap<>();

    /// Creates a new unpublished level
    /// @spec.requires title, description, creatorId not to be null
    /// @spec.effects creates a Level with the given title, description, and user.
    ///                 published is set to false, width to 256, height to 14,
    ///                 and clearCondition with NoCondition and 0 target value
    ///                 objectLayer, and worldLayer are initialized as empty maps.
    /// @param title a string that represents the name of the level
    /// @param description short description of the level
    /// @param user the user instance of the user creating the level
    public Level(String title, String description, User user) {
        this.title = title;
        this.description = description;
        this.published = false;
        this.creator = user;
        this.clearCondition = new ClearCondition(new NoCondition(), 0);
    }

    /// Persistence constructor used by Spring Data MongoDB to recreate a Level from the database.
    /// @param creator the user who created this level
    /// @param title the name of the level
    /// @param description a short description of the level
    /// @param published whether the level is publicly available
    /// @param clearCondition the condition required to clear the level
    /// @param objectLayer a map of positions to game objects in the object layer
    /// @param worldLayer a map of positions to ground objects in the world layer
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


    /// Creates a new unpublished level with pre-existing object and world layers.
    /// @param title the name of the level
    /// @param description a short description of the level
    /// @param creator the user who will own this level
    /// @param objectLayer a map of positions to game objects to copy into the object layer
    /// @param worldLayer a map of positions to ground objects to copy into the world layer
    ///
    private Level(String title, String description, User creator,  HashMap<Position, GameObject> objectLayer, HashMap<Position, GroundObject> worldLayer, ClearCondition clearCondition) {
        this(title, description, creator);
        this.objectLayer.putAll(objectLayer);
        this.worldLayer.putAll(worldLayer);
    }

    /// Creates a copy of the level for a given creator
    /// @spec.requires creator not to be null
    /// @param creator a User instance
    /// @return a new Level with the same title, description,
    ///               clearCondition, objectLayer, and worldLayer as this level,
    ///               but with the given creator and published set to false.
    ///
    public Level cloneFor(User creator) { return new Level(this.title, this.description, creator, this.objectLayer, this.worldLayer, this.clearCondition); }

    /// @return a boolean instance "published"
    public boolean isPublished() { return this.published; }

    /// @return the unique identifier of this level.
    public String getId() {
        return id;
    }

    /// @return the title of this level.
    public String getTitle() {
        return title;
    }

    /// @return the description of this level.
    public String getDescription() {
        return description;
    }

    /// @return the creator of this level.
    public User getCreator() {
        return creator;
    }

    /// @return the clear condition of this level.
    public ClearCondition getClearCondition() {
        return clearCondition;
    }

    /// @return an unmodifiable view of the object layer of this level.
    public Map<Position, GameObject> getObjectLayer() {
        return Collections.unmodifiableMap(objectLayer);
    }

    /// @return an unmodifiable view of the world layer of this level.
    public Map<Position, GroundObject> getWorldLayer() {
        return Collections.unmodifiableMap(worldLayer);
    }

    /// Adds or replaces a game object at the given position in the object layer.
    /// @param pos the position at which to place the game object
    /// @param gameObject the game object to place
    public void putObjectLayer(Position pos, GameObject gameObject) {
        this.objectLayer.put(pos, gameObject);
    }

    /// Adds or replaces a ground object at the given position in the world layer.
    /// @param pos the position at which to place the ground object
    /// @param groundObject the ground object to place
    public void putWorldLayer(Position pos, GroundObject groundObject) {
        this.worldLayer.put(pos, groundObject);
    }

    /// @return width of the map
    public int getWidth(){
        return width;
    }

    /// @return height of the map
    public int getHeight(){
        return height;
    }

    /// Sets the title of this level.
    /// @spec.requires title is not null.
    /// @spec.modifies this.
    /// @spec.effects sets the title of this level to the given title.
    /// @param title the new title of this level.
    public void setTitle(String title) { this.title = title; }

    /// Sets the description of this level.
    /// @spec.requires description is not null.
    /// @spec.modifies this.
    /// @spec.effects sets the description of this level to the given description.
    /// @param description the new description of this level.
    public void setDescription(String description) { this.description = description; }

    /// Sets the published status of this level.
    /// @spec.modifies this.
    /// @spec.effects sets the published status of this level to the given value.
    /// @param published the new published status of this level.
    public void setPublished(boolean published) { this.published = published; }

    /// Sets the clear condition of this level.
    /// @spec.requires clearCondition is not null.
    /// @spec.modifies this.
    /// @spec.effects sets the clear condition of this level to the given value.
    /// @param clearCondition the new clear condition of this level.
    public void setClearCondition(ClearCondition clearCondition) { this.clearCondition = clearCondition; }
}
