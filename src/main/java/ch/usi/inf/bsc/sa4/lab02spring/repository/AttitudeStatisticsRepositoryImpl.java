package ch.usi.inf.bsc.sa4.lab02spring.repository;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.LevelAttitudeType;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

/// MongoDB-backed implementation of custom attempt statistics queries.
@Repository
public class AttitudeStatisticsRepositoryImpl implements AttitudeStatisticsRepository {

    /// Spring Data MongoDB template used to run aggregation queries.
    private final MongoTemplate mongoTemplate;

    /// Creates a statistics repository backed by the provided Mongo template.
    /// @param mongoTemplate the template used to query the database
    public AttitudeStatisticsRepositoryImpl(final MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public long countByLevelAttitude(final Level level, final LevelAttitudeType attitude) {
        final Query q = Query.query(
            Criteria.where("level.$id").is(new ObjectId(level.getId()))
            .and("attitude").is(attitude.name()));
        return this.mongoTemplate.count(q, "ratings");
        
    }

    
}
