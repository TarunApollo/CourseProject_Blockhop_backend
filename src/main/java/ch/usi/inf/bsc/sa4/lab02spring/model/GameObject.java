package ch.usi.inf.bsc.sa4.lab02spring.model;

/// Common contract for all game objects placed in a level.
/// Every game object has a global tile identifier and a grid position.
public sealed interface GameObject permits Enemy, Item {
    /// Returns the global tile identifier of this object.
    /// @return the object GID
    int gid();

    /// Returns the grid position of this object.
    /// @return the object's position
    Position pos();
}
