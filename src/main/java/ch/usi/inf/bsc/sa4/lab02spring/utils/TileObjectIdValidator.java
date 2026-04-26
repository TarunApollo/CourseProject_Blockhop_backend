package ch.usi.inf.bsc.sa4.lab02spring.utils;

import java.util.function.IntPredicate;

/**
 * Validates tile GIDs. Throws IllegalArgumentException for invalid values.
 * Value 0 means "no tile" (removal marker) and is always valid.
 */
public final class TileObjectIdValidator {

    private TileObjectIdValidator() {}  // not instantiable

    public static void validate(final int value, final IntPredicate validator) {
        if (value != 0 && !validator.test(value)) {
            throw new IllegalArgumentException("Invalid GID: " + value);
        }
    }
}

