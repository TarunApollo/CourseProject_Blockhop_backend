package ch.usi.inf.bsc.sa4.lab02spring.repository;

import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

/// MongoDB-backed implementation of custom attempt statistics queries.
@Repository
public class AttemptStatisticsRepositoryImpl implements AttemptStatisticsRepository {

    /// Spring Data MongoDB template used to run aggregation queries.
    private final MongoTemplate mongoTemplate;

    /// Creates a statistics repository backed by the provided Mongo template.
    /// @param mongoTemplate the template used to query the database
    public AttemptStatisticsRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public long countDistinctPlayedLevelsByUser(User user) {
        return countDistinctLevelsByUser(user, false);
    }

    @Override
    public long countDistinctCompletedLevelsByUser(User user) {
        return countDistinctLevelsByUser(user, true);
    }

    private long countDistinctLevelsByUser(User user, boolean completedOnly) {
        Criteria criteria = Criteria.where("user.$id").is(user.getId());
        if (completedOnly) {
            criteria = criteria.and("completed").is(true);
        }

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("level.$id"),
                Aggregation.count().as("count")
        );

        Document result = mongoTemplate
                .aggregate(aggregation, "attempts", Document.class)
                .getUniqueMappedResult();

        if(result == null)return 0;
        Number count = (Number) result.get("count");
        return count == null ? 0 : count.longValue();

    }
}
