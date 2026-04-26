package ch.usi.inf.bsc.sa4.lab02spring.utils;

import java.util.function.IntPredicate;

/**
 * Validates tile GIDs. Throws IllegalArgumentException for invalid values.
 * Value 0 means "no tile" (removal marker) and is always valid.
 */
public final class TileObjectIdValidator {

    /** Utility class; not instantiable. */
    private TileObjectIdValidator() {}

    /**
     * Throws IllegalArgumentException if the given GID is non-zero and
     * rejected by the validator.
     *
     * @param value     the GID to validate; 0 is always accepted (removal marker)
     * @param validator predicate used to test non-zero GIDs
     */
    public static void validate(final int value, final IntPredicate validator) {
        if (value != 0 && !validator.test(value)) {
            throw new IllegalArgumentException("Invalid GID: " + value);
        }
    }
}

