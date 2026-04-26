package ch.usi.inf.bsc.sa4.lab02spring.model;

import java.util.function.IntPredicate;

/**
 * Value object representing a game/ground tile identifier for world layer operations.
 *
 * <p>IMPORTANT: The canonical constructor {@code new TileObjectId(int)} bypasses validation
 * and should NOT be used directly by clients. Always use one of:
 * <ul>
 *   <li>{@link #TileObjectId(int, IntPredicate)} - for validated tile IDs</li>
 *   <li>{@link #remove()} - for creating a removal marker</li>
 * </ul>
 *
 * <p>Using the canonical constructor directly may result in invalid GIDs being
 * constructed. It is the caller's responsibility to use the validated constructor.
 *
 * <p><b>Canonical constructor contract:</b> performs NO validation.
 * Caller must ensure value is valid or 0. Use {@link #TileObjectId(int, IntPredicate)} instead.
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
public record TileObjectId(int value) {

    /**
     * Constructs a validated TileObjectId.
     *
     * @param value the GID value; must be 0 (removal) or a valid ground tile GID
     * @param validator predicate to validate the GID; must not be null
     */
    public TileObjectId(final int value, final IntPredicate validator) {
        this(value);
        if (value != 0 && !validator.test(value)) {
            throw new IllegalArgumentException("Invalid ground GID: " + value);
        }
    }

    /**
     * Creates a TileObjectId representing a removal operation.
     *
     * <p>No validation needed since value is 0 (reserved for "no tile").
     *
     * @return a TileObjectId with value 0, indicating tile removal
     */
    public static TileObjectId remove() {
        return new TileObjectId(0);
    }

    /**
     * Checks if this TileObjectId represents a removal operation.
     *
     * @return true if value is 0, false otherwise
     */
    public boolean isRemoval() {
        return this.value == 0;
    }
}
