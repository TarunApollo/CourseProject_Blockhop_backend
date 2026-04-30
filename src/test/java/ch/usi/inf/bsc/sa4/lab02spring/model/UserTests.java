package ch.usi.inf.bsc.sa4.lab02spring.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Tests for [User] model value-based equality.
@DisplayName("The User Model")
class UserTests {

    /// Verifies reflexivity.
    @Test
    @DisplayName("equals is reflexive")
    void testReflexive() {
        final User user = new User("u1", "N");
        Assertions.assertEquals(user, user);
    }

    /// Verifies null handling.
    @Test
    @DisplayName("equals returns false for null")
    void testNull() {
        final User user = new User("u1", "N");
        Assertions.assertNotEquals(null, user);
    }

    /// Verifies different types.
    @Test
    @DisplayName("equals returns false for different types")
    void testType() {
        final User user = new User("u1", "N");
        Assertions.assertNotEquals("string", user);
    }

    /// Verifies identical objects.
    @Test
    @DisplayName("equals returns true for identical fields")
    void testIdentical() {
        final User u1 = new User("u1", "N");
        final User u2 = new User("u1", "N");
        Assertions.assertEquals(u1, u2);
    }

    /// Verifies different IDs.
    @Test
    @DisplayName("equals returns false for different ids")
    void testDiffId() {
        final User u1 = new User("u1", "N");
        final User u2 = new User("u2", "N");
        Assertions.assertNotEquals(u1, u2);
    }

    /// Verifies different names.
    @Test
    @DisplayName("equals returns false for different names")
    void testDiffName() {
        final User u1 = new User("u1", "N1");
        final User u2 = new User("u1", "N2");
        Assertions.assertNotEquals(u1, u2);
    }

    /// Verifies hashCode consistency.
    @Test
    @DisplayName("hashCode is consistent with equals")
    void testHash() {
        final User u1 = new User("u1", "N");
        final User u2 = new User("u1", "N");
        Assertions.assertEquals(u1.hashCode(), u2.hashCode());
    }
}
