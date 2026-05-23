package ch.usi.inf.bsc.sa4.lab02spring.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests for [SpriteCatalogService].
@DisplayName("SpriteCatalogService Tests")
class SpriteCatalogServiceTests {
    /// Key for the frames property in the payload.
    private static final String FRAMES_KEY = "frames";
    /// Name of the idle character sprite.
    private static final String CHARACTER_IDLE = "character_beige_idle";

    /// The service under test.
    @Autowired
    private SpriteCatalogService spriteCatalogService;

    /// Sets up the service before each test.

    @Test
    @DisplayName("loads and parses characters spritesheet correctly")
    void loadsCharactersSpritesheet() {
        final Map<String, Object> characters = spriteCatalogService.getPayload("characters");
        assertNotNull(characters, "Characters payload should not be null");
        assertTrue(characters.containsKey(FRAMES_KEY), "Payload should contain frames");
        
        @SuppressWarnings("unchecked")
        final Map<String, Object> frames = (Map<String, Object>) characters.get(FRAMES_KEY);
        assertTrue(frames.containsKey(CHARACTER_IDLE), "Should contain character_beige_idle");
    }

    @Test
    @DisplayName("validates sprite names correctly")
    void validatesSpriteNames() {
        assertTrue(spriteCatalogService.isValidSprite(CHARACTER_IDLE));
        assertFalse(spriteCatalogService.isValidSprite("non_existent_sprite"));
    }

    @Test
    @DisplayName("returns null for unknown spritesheet type")
    void returnsNullForUnknownType() {
        assertNull(spriteCatalogService.getPayload("unknown_type"));
    }
}
