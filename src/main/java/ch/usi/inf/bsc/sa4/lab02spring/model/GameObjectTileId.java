package ch.usi.inf.bsc.sa4.lab02spring.model;

import java.util.function.IntPredicate;

/**
 * Value object representing a tile identifier for both world layer and object layer operations.
 *
 * <p>IMPORTANT: The canonical constructor {@code new GameObjectTileId(int)} bypasses validation
 * and should NOT be used directly by clients. Always use one of:
 * <ul>
 *   <li>{@link #GameObjectTileId(int, IntPredicate)} - for validated tile IDs</li>
 *   <li>{@link #remove()} - for creating a removal marker</li>
 * </ul>
 *
 * <p>Using the canonical constructor directly may result in invalid GIDs being
 * constructed. It is the caller's responsibility to use the validated constructor.
 *
 * <p><b>Canonical constructor contract:</b> performs NO validation.
 * Caller must ensure value is valid or 0. Use {@link #GameObjectTileId(int, IntPredicate)} instead.
 *
 * <p><b>Why it's cool:</b> Self-validating (when using the two-argument constructor).
 * This keeps the domain model pure by ensuring entities don't need to validate
 * GIDs or depend on application services.
 *
 * <p><b>Pros for DDD design:</b>
 * <ul>
 *   <li>Value objects should enforce their own invariants at construction time</li>
 *   <li>Invalid states should be unrepresentable (when using validated constructor)</li>
 *   <li>Validation logic lives at the boundary (service layer), not in entities</li>
 *   <li>Entity methods receive already-validated domain objects, keeping them pure</li>
 * </ul>
 *
 *
 * @param value the GID value; caller using canonical constructor must ensure it is valid or 0
 */
public record GameObjectTileId(int value) {

    /**
     * Constructs a validated GameObjectTileId.
     *
     * @param value the GID value; must be 0 (removal) or a valid tile GID
     * @param validator predicate to validate the GID; must not be null
     */
    public GameObjectTileId(int value, IntPredicate validator) {
        this(value);
        if (value != 0 && !validator.test(value)) {
            throw new IllegalArgumentException("Invalid GID: " + value);
        }
    }

    /**
     * Creates a GameObjectTileId representing a removal operation.
     *
     * <p>No validation needed since value is 0 (reserved for "no tile").
     *
     * @return a GameObjectTileId with value 0, indicating tile removal
     */
    public static GameObjectTileId remove() {
        return new GameObjectTileId(0);
    }

    /**
     * Checks if this GameObjectTileId represents a removal operation.
     *
     * @return true if value is 0, false otherwise
     */
    public boolean isRemoval() {
        return this.value == 0;
    }
}
