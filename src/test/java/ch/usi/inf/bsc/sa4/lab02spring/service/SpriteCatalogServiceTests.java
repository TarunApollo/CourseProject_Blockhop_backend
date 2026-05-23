package ch.usi.inf.bsc.sa4.lab02spring.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;
import java.util.Optional;

/// Tests for the [SpriteCatalogService].
@SpringBootTest
@DisplayName("The SpriteCatalog Service")
class SpriteCatalogServiceTests {

    /// The service under test.
    @Autowired
    private SpriteCatalogService spriteCatalogService;

    /// Key for the frames property in the payload.
    private static final String FRAMES_KEY = "frames";
    /// Name of the idle character sprite.
    private static final String CHARACTER_IDLE = "character_beige_idle";
    /// Valid existing spritesheet type.
    private static final String TYPE_CHARACTERS = "characters";
    /// Unknown spritesheet type.
    private static final String TYPE_UNKNOWN = "unknown_type";
    /// Non-existent sprite.
    private static final String NON_EXISTENT_SPRITE = "non_existent_sprite";

    /// Tests for loading catalog payloads.
    @Nested
    @DisplayName("when loading payload")
    class LoadPayload {

        @Test
        @DisplayName("loads and parses characters spritesheet correctly")
        void loadsCharactersSpritesheet() {
            final Optional<Map<String, Object>> charactersOpt = spriteCatalogService.getPayload(TYPE_CHARACTERS);
            Assertions.assertTrue(charactersOpt.isPresent(), "Characters payload should be present");
            
            final Map<String, Object> characters = charactersOpt.get();
            Assertions.assertTrue(characters.containsKey(FRAMES_KEY), "Payload should contain frames");
            
            @SuppressWarnings("unchecked")
            final Map<String, Object> frames = (Map<String, Object>) characters.get(FRAMES_KEY);
            Assertions.assertTrue(frames.containsKey(CHARACTER_IDLE), "Should contain character_beige_idle");
        }

        @Test
        @DisplayName("returns empty optional for unknown spritesheet type")
        void returnsEmptyForUnknownType() {
            Assertions.assertTrue(spriteCatalogService.getPayload(TYPE_UNKNOWN).isEmpty());
        }
    }

    /// Tests for validating sprite names.
    @Nested
    @DisplayName("when validating sprite names")
    class ValidateSprite {

        @Test
        @DisplayName("validates sprite names correctly")
        void validatesSpriteNames() {
            Assertions.assertTrue(spriteCatalogService.isValidSprite(CHARACTER_IDLE));
        }

        @Test
        @DisplayName("returns false for non-existent sprite")
        void invalidatesSpriteNames() {
            Assertions.assertFalse(spriteCatalogService.isValidSprite(NON_EXISTENT_SPRITE));
        }
    }
}
