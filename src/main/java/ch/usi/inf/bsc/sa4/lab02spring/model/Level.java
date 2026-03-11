package ch.usi.inf.bsc.sa4.lab02spring.model;

import org.springframework.data.annotation.Id;
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

    /// Creates a new unpublished level
    /// @spec.requires title, description, creatorId not to be null
    /// @spec.effects creates a Level with the given title, description, and creatorId.
    ///                 published is set to false, width to 256, height to 14,
    ///                 and clearCondition to ClearCondition(NONE, 0).
    ///                 timesPlayed, objectLayer, and worldLayer are initialized as empty maps.
    /// @param title a string that represents the name of the level
    /// @param description short description of the level
    /// @param creatorId the id of the user creating the level
    ///
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

    /// Creates a copy of the level for a given creator
    /// @spec.requires newCreatorId not to be null
    /// @param newCreatorId a string with a new creatorId
    /// @returns returns a new Level with the same title, description,
    ///               clearCondition, objectLayer, and worldLayer as this level,
    ///               but with the given newCreatorId and published set to false.
    public Level cloneFor(String newCreatorId) {
        Level clone = new Level(this.title, this.description, newCreatorId);
        clone.clearCondition = this.clearCondition;
        clone.objectLayer = new HashMap<>(this.objectLayer);
        clone.worldLayer = new HashMap<>(this.worldLayer);
        return clone;
    }

    /// @returns a boolean instance "published"
    public boolean isPublished(){
        return this.published;
    }

    /// @returns the unique identifier of this level.
    public String getId() {
        return id;
    }

    /// @returns the title of this level.
    public String getTitle() {
        return title;
    }

    /// @returns the description of this level.
    public String getDescription() {
        return description;
    }

    /// @returns the creator id of this level.
    public String getCreatorId() {
        return creatorId;
    }

    /// @returns the clear condition of this level.
    public ClearCondition getClearCondition() {
        return clearCondition;
    }

    /// @returns a map of player ids to the dates they played this level.
    public Map<String, Date> getTimesPlayed() {
        return timesPlayed;
    }

    /// @returns an unmodifiable view of the object layer of this level.
    public Map<Position, GameObject> getObjectLayer() {
        return Collections.unmodifiableMap(objectLayer);
    }

    /// @returns an unmodifiable view of the world layer of this level.
    public Map<Position, GroundObject> getWorldLayer() {
        return Collections.unmodifiableMap(worldLayer);
    }

    /// Sets the title of this level.
    /// @spec.requires title is not null.
    /// @spec.modifies this.
    /// @spec.effects sets the title of this level to the given title.
    /// @param title the new title of this level.
    public void setTitle(String title) {
        this.title = title;
    }

    /// Sets the description of this level.
    /// @spec.requires description is not null.
    /// @spec.modifies this.
    /// @spec.effects sets the description of this level to the given description.
    /// @param description the new description of this level.
    public void setDescription(String description) {
        this.description = description;
    }

    /// Sets the published status of this level.
    /// @spec.modifies this.
    /// @spec.effects sets the published status of this level to the given value.
    /// @param published the new published status of this level.
    public void setPublished(boolean published) {
        this.published = published;
    }

    /// Sets the clear condition of this level.
    /// @spec.requires clearCondition is not null.
    /// @spec.modifies this.
    /// @spec.effects sets the clear condition of this level to the given value.
    /// @param clearCondition the new clear condition of this level.
    public void setClearCondition(ClearCondition clearCondition) {
        this.clearCondition = clearCondition;
    }
}
