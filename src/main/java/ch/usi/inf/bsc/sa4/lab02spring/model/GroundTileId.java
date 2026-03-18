package ch.usi.inf.bsc.sa4.lab02spring.model;

import java.util.function.IntPredicate;

/**
 * Value object representing a ground tile identifier for world layer operations.
 *
 * Why it's cool: Self-validating -> cannot be constructed with an invalid GID.
 * This keeps the domain model pure by ensuring entities don't need to validate 
 * GIDs or depend on application services
 *
 * Pros for ddd design:
 * - Value objects should enforce their own invariants at construction time
 * - Invalid states should be unrepresentable (can't construct invalid GroundTileId)
 * - Validation logic lives at the boundary (service layer), not in entities
 * - Entity methods receive already-validated domain objects, keeping them pure
 *
 * Usage:
 * - GroundTileId.remove() creates a removal marker (value = 0)
 * - new GroundTileId(gid, validator::isGroundGID) creates a tile with validation
 * - gid == 0 means "remove the tile at this position"
 * - gid > 0 means "add/replace tile at this position"
 */

public record GroundTileId(int value) {

    
    // Validates that the GID represents a valid ground tile (or 0 for removal).
    //
    // @param value the GID value (must be valid ground tile, or 0 for removal)
    // @param validator predicate that returns true if GID is a valid ground tile
    // @throws IllegalArgumentException if GID is not a valid ground tile (and not 0)
    //
    public GroundTileId(int value, IntPredicate validator) {
        this(value);
        if (value != 0 && !validator.test(value)) {
            throw new IllegalArgumentException("Invalid ground GID: " + value);
        }
    }

    // Creates a GroundTileId representing a removal operation.
    // No validation needed since value is 0 (reserved for "no tile").
    //
    // @return a GroundTileId with value 0, indicating tile removal
    public static GroundTileId remove() {
        return new GroundTileId(0);
    }

    //
    // Checks if this GroundTileId represents a removal operation.
    // @return true if this is a removal (value == 0), false otherwise
    //
    public boolean isRemoval() {
        return this.value == 0;
    }
}