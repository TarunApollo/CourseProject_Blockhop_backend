package ch.usi.inf.bsc.sa4.lab02spring.repository;

import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.LevelAttitudeType;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

/// MongoDB-backed implementation of custom level attitude statistics queries.
@Repository
public class AttitudeStatisticsRepositoryImpl implements AttitudeStatisticsRepository {

    /// Spring Data MongoDB template used to run count queries.
    private final MongoTemplate mongoTemplate;

    /// Creates a statistics repository backed by the provided Mongo template.
    /// @param mongoTemplate the template used to query the database
    public AttitudeStatisticsRepositoryImpl(final MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public long countLikesByLevel(final Level level) {
        return countByLevelAndAttitude(level, LevelAttitudeType.LIKE);
    }

    @Override
    public long countDislikesByLevel(final Level level) {
        return countByLevelAndAttitude(level, LevelAttitudeType.DISLIKE);
    }

    private long countByLevelAndAttitude(final Level level, final LevelAttitudeType attitudeType) {
        final Criteria criteria = Criteria.where("level.$id").is(level.getId())
                .and("attitude").is(attitudeType);
        return mongoTemplate.count(Query.query(criteria), "ratings");
    }
}