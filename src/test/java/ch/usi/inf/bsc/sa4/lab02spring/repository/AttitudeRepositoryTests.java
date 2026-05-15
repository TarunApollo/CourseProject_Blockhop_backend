package ch.usi.inf.bsc.sa4.lab02spring.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.repository.Query;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("AttitudeRepository annotation checks")
class AttitudeRepositoryTests {

    @Test
    @DisplayName("findByLevelIdAndUserId should have a Query annotation with expected JSON")
    void queryAnnotationPresent() throws NoSuchMethodException {
        final Method m = AttitudeRepository.class.getMethod("findByLevelIdAndUserId", String.class, String.class);
        final Query q = m.getAnnotation(Query.class);
        assertNotNull(q, "Expected @Query on findByLevelIdAndUserId");
        assertEquals("{ 'level.id': ?0, 'user.id': ?1 }", q.value());
    }
}
