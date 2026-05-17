package ch.usi.inf.bsc.sa4.lab02spring.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.lang.reflect.Method;

import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;

/// Unit tests for AttitudeRepository
@DisplayName("AttitudeRepository method checks")
class AttitudeRepositoryTests {

    @Test
    @DisplayName("findByLevelAndUser exists with correct signature")
    void findByLevelAndUserExists() throws NoSuchMethodException {
        final Method m = AttitudeRepository.class.getMethod("findByLevelAndUser", Level.class, User.class);
        Assertions.assertNotNull(m, "Expected method findByLevelAndUser(Level, User) to be present");
    }

    @Test
    @DisplayName("deleteByLevelAndUser exists with correct signature")
    void deleteByLevelAndUserExists() throws NoSuchMethodException {
        final Method m = AttitudeRepository.class.getMethod("deleteByLevelAndUser", Level.class, User.class);
        Assertions.assertNotNull(m, "Expected method deleteByLevelAndUser(Level, User) to be present");
    }

    @Test
    @DisplayName("deleteByLevel_Id exists with correct signature")
    void deleteByLevelIdExists() throws NoSuchMethodException {
        final Method m = AttitudeRepository.class.getMethod("deleteByLevel_Id", String.class);
        Assertions.assertNotNull(m, "Expected method deleteByLevel_Id(String) to be present");
    }
}
