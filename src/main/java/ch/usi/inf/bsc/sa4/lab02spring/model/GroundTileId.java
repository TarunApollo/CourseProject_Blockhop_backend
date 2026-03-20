package ch.usi.inf.bsc.sa4.lab02spring.model;

import java.util.function.IntPredicate;

/**
 * Value object representing a ground tile identifier for world layer operations.
 *
 * <p>IMPORTANT: The canonical constructor {@code new GroundTileId(int)} bypasses validation
 * and should NOT be used directly by clients. Always use one of:
 * <ul>
 *   <li>{@link #GroundTileId(int, IntPredicate)} - for validated tile IDs</li>
 *   <li>{@link #remove()} - for creating a removal marker</li>
 * </ul>
 *
 * <p>Using the canonical constructor directly may result in invalid GIDs being
 * constructed. It is the caller's responsibility to use the validated constructor.
 *
 * <p><b>Canonical constructor contract:</b> performs NO validation.
 * Caller must ensure value is valid or 0. Use {@link #GroundTileId(int, IntPredicate)} instead.
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
public record GroundTileId(int value) {

    /**
     * Constructs a validated GroundTileId.
     *
     * @param value the GID value; must be 0 (removal) or a valid ground tile GID
     * @param validator predicate to validate the GID; must not be null
     * @requires value == 0 || validator.test(value)
     * @requires validator != null
     */
    public GroundTileId(int value, IntPredicate validator) {
        this(value);
        if (value != 0 && !validator.test(value)) {
            throw new IllegalArgumentException("Invalid ground GID: " + value);
        }
    }

    /**
     * Creates a GroundTileId representing a removal operation.
     *
     * <p>No validation needed since value is 0 (reserved for "no tile").
     *
     * @return a GroundTileId with value 0, indicating tile removal
     */
    public static GroundTileId remove() {
        return new GroundTileId(0);
    }

    /**
     * Checks if this GroundTileId represents a removal operation.
     *
     * @return true if value is 0, false otherwise
     */
    public boolean isRemoval() {
        return this.value == 0;
    }
}
