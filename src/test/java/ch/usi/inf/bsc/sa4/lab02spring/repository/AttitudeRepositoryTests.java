package ch.usi.inf.bsc.sa4.lab02spring.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.data.mongodb.repository.Query;

import java.lang.reflect.Method;

/// Unit tests for AttitudeRepository
@DisplayName("AttitudeRepository annotation checks")
class AttitudeRepositoryTests {

    /// Verifies that findByLevelIdAndUserId has the correct @Query annotation.
    @Test
    @DisplayName("findByLevelIdAndUserId should have a Query annotation with expected JSON")
    void queryAnnotationPresent() throws NoSuchMethodException {
        final Method m = AttitudeRepository.class.getMethod("findByLevelIdAndUserId", String.class, String.class);
        final Query q = m.getAnnotation(Query.class);
        Assertions.assertNotNull(q, "Expected @Query on findByLevelIdAndUserId");
        Assertions.assertEquals("{ 'level.id': ?0, 'user.id': ?1 }", q.value());
    }
}
