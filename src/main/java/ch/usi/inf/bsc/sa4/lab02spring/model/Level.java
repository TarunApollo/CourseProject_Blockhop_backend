package ch.usi.inf.bsc.sa4.lab02spring.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.mongodb.core.mapping.Document;

import ch.usi.inf.bsc.sa4.lab02spring.utils.ForbiddenUserException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.LevelPublishedException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.ObjectPlacementConflictException;


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
    private final Map<Position, GameObject> objectLayer = new HashMap<>();
    private final Map<Position, GroundObject> worldLayer = new HashMap<>();

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
        this.clearCondition = new ClearCondition(new Condition.NoClearCondition(), 0);
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
    private Level(String title, String description, User creator,  Map<Position, GameObject> objectLayer, Map<Position, GroundObject> worldLayer, ClearCondition clearCondition) {
        this(title, description, creator);
        this.clearCondition = clearCondition;
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


    /// remove a ground object from the world layer.
    public void removeGroundObject(Position pos){
        this.worldLayer.remove(pos);
    }

    /// remove a game object from the objectLayer(why on earth do we call it the object layer if they are all objects??)
    public void removeObjectLayer(Position pos) {
        this.objectLayer.remove(pos);
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
                String.format("Position (%d, %d) is out of bounds. Valid range: x=[0,%d], y=[0,%d]",
                    position.x(), position.y(), this.width - 1, this.height - 1)
            );
        }
    }

    public void ensureObjectCanBePlacedAt(Position position)
    {
        this.ensureWithinBounds(position);
        if(this.worldLayer.containsKey(position) || this.objectLayer.containsKey(position)){
            throw new ObjectPlacementConflictException();
        }
    }

    // TODO: docs
    public void updateWorldLayerTile(Position position, TileObjectId gid) {
        this.ensureWithinBounds(position);

        if (gid.isRemoval()) {
            this.worldLayer.remove(position);
        } else {
            this.worldLayer.put(position, new GroundObject(gid.value()));
        }
    }

    // TODO: docs
    public void updateWorldLayerBatch(Map<Position, TileObjectId> tiles) {
        for (Position pos : tiles.keySet()) {
            this.ensureWithinBounds(pos);
        }

        // All positions valid? --> apply all mutations
        for (Map.Entry<Position, TileObjectId> entry : tiles.entrySet()) {
            Position pos = entry.getKey();
            TileObjectId gid = entry.getValue();

            if (gid.isRemoval()) {
                this.worldLayer.remove(pos);
            } else {
                this.worldLayer.put(pos, new GroundObject(gid.value()));
            }
        }
    }


}
