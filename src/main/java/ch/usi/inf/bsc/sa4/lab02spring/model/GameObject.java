package ch.usi.inf.bsc.sa4.lab02spring.model;

/// Common contract for all game objects placed in a level. Every game object
/// has a catalog tile identifier and a grid position.
public sealed interface GameObject permits Enemy, Item {
    /// Returns the catalog tile identifier of this object.
    /// 
    /// @return the object tile id
    String tileId();

    /// Returns the grid position of this object.
    /// 
    /// @return the object's position
    Position pos();
}
