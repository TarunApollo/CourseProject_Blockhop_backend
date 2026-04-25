package ch.usi.inf.bsc.sa4.lab02spring.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the TileSetService.
 * Goal: Verify that the tileset is loaded correctly and GID classification works.
 */
@DisplayName("The TileSet Service (Unit)")
@SuppressWarnings({ "PMD.AtLeastOneConstructor", "NullAway" })
/* default */ class TileSetServiceTests {

    /** GID that is not expected to exist in the tileset. */
    private static final int INVALID_GID = 0;

    /** GID unlikely to be present in any valid tileset. */
    private static final int UNKNOWN_GID = 99999;

    /** The service under test. */
    private TileSetService tileSetService;

    /**
     * Instantiates the service before each test.
     */
    @BeforeEach
    /* default */ void setup() {
        this.tileSetService = new TileSetService();
    }

    /**
     * Tests for the getTileSet method.
     */
    @Nested
    @DisplayName("when getting the tileset")
    @SuppressWarnings("PMD.AtLeastOneConstructor")
    /* default */ class GetTileSet {

        /**
         * Verifies that the returned tileset is not null.
         */
        @Test
        @DisplayName("should return a non-null tileset after loading")
        /* default */ void testGetTileSetNotNull() {
            assertNotNull(tileSetService.getTileSet());
        }

        /**
         * Verifies that the tileset contains a non-null tile list.
         */
        @Test
        @DisplayName("should return a tileset with a non-null tile list")
        /* default */ void testGetTileSetHasTiles() {
            assertNotNull(tileSetService.getTileSet().tiles());
        }
    }

    /**
     * Tests for the isGroundGID method.
     */
    @Nested
    @DisplayName("when checking ground GIDs")
    @SuppressWarnings("PMD.AtLeastOneConstructor")
    /* default */ class IsGroundGID {

        /**
         * Verifies that GID 0 is not classified as a ground tile.
         */
        @Test
        @DisplayName("should return false for GID 0")
        /* default */ void testIsGroundGIDReturnsFalseForZero() {
            assertFalse(tileSetService.isGroundGID(INVALID_GID));
        }
    }

    /**
     * Tests for the isObjectGID method.
     */
    @Nested
    @DisplayName("when checking object GIDs")
    @SuppressWarnings("PMD.AtLeastOneConstructor")
    /* default */ class IsObjectGID {

        /**
         * Verifies that GID 0 is not classified as an object tile.
         */
        @Test
        @DisplayName("should return false for GID 0")
        /* default */ void testIsObjectGIDReturnsFalseForZero() {
            assertFalse(tileSetService.isObjectGID(INVALID_GID));
        }
    }

    /**
     * Tests for the getObjectTileType method.
     */
    @Nested
    @DisplayName("when getting the tile type for a GID")
    @SuppressWarnings("PMD.AtLeastOneConstructor")
    /* default */ class GetObjectTileType {

        /**
         * Verifies that an unknown GID returns an empty string.
         */
        @Test
        @DisplayName("should return an empty string for an unknown GID")
        /* default */ void testGetObjectTileTypeUnknownGID() {
            assertEquals("", tileSetService.getObjectTileType(UNKNOWN_GID));
        }
    }
}
