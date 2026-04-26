package ch.usi.inf.bsc.sa4.lab02spring.repository;

import ch.usi.inf.bsc.sa4.lab02spring.model.User;

import org.bson.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;

/**
 * Unit tests for AttemptStatisticsRepositoryImpl.
 * MongoTemplate is mocked; aggregation pipeline is not exercised against a real database.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AttemptStatisticsRepositoryImpl")
@SuppressWarnings({"PMD.AtLeastOneConstructor", "NullAway"})
/* package */ class AttemptStatisticsRepositoryImplTests {

    /** Mocked Mongo template. */
    @Mock
    private MongoTemplate mongoTemplate;

    /** Mocked aggregation results returned by mongoTemplate.aggregate(). */
    @Mock
    private AggregationResults<Document> aggregationResults;

    /** Repository under test. */
    private AttemptStatisticsRepositoryImpl repository;

    /** A test user used across tests. */
    private User testUser;

    @BeforeEach
    void setup() {
        repository = new AttemptStatisticsRepositoryImpl(mongoTemplate);
        testUser = new User("user-1", "Mario");
        Mockito.when(mongoTemplate.aggregate(
                Mockito.any(Aggregation.class),
                Mockito.eq("attempts"),
                Mockito.eq(Document.class)))
                .thenReturn(aggregationResults);
    }

    @Test
    @DisplayName("countDistinctPlayedLevelsByUser returns the count when aggregation succeeds")
    void playedReturnsCount() {
        Mockito.when(aggregationResults.getUniqueMappedResult())
                .thenReturn(new Document("count", 5));

        Assertions.assertEquals(5L, repository.countDistinctPlayedLevelsByUser(testUser));
    }

    @Test
    @DisplayName("countDistinctCompletedLevelsByUser returns the count when aggregation succeeds")
    void completedReturnsCount() {
        Mockito.when(aggregationResults.getUniqueMappedResult())
                .thenReturn(new Document("count", 3));

        Assertions.assertEquals(3L, repository.countDistinctCompletedLevelsByUser(testUser));
    }

    @Test
    @DisplayName("returns 0 when aggregation result is null")
    void nullResultReturnsZero() {
        Mockito.when(aggregationResults.getUniqueMappedResult()).thenReturn(null);

        Assertions.assertEquals(0L, repository.countDistinctPlayedLevelsByUser(testUser));
    }

    @Test
    @DisplayName("returns 0 when aggregation result has no count field")
    void missingCountFieldReturnsZero() {
        Mockito.when(aggregationResults.getUniqueMappedResult())
                .thenReturn(new Document());

        Assertions.assertEquals(0L, repository.countDistinctPlayedLevelsByUser(testUser));
    }
}
