package ch.usi.inf.bsc.sa4.lab02spring.model;

import java.time.Instant;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Unit tests for [LevelFavorite].
@DisplayName("The LevelFavorite Model")
@SuppressWarnings("NullAway")
class LevelFavoriteTests {

    private User testUser;
    private Level testLevel;

    @BeforeEach
    void setup() {
        this.testUser = new User("user-1", "Mario");
        this.testLevel = new Level("Test Level", "desc", this.testUser);
    }

    @Test
    @DisplayName("persistence constructor sets id and exposes all fields via getters")
    void persistenceConstructorSetsAllFields() {
        final Instant ts = Instant.now();
        final LevelFavorite fav = new LevelFavorite("fav-1", testUser, testLevel, ts);

        Assertions.assertEquals("fav-1", fav.getId());
        Assertions.assertSame(testUser, fav.getUser());
        Assertions.assertSame(testLevel, fav.getLevel());
        Assertions.assertSame(ts, fav.getTimestamp());
    }
}