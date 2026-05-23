package ch.usi.inf.bsc.sa4.lab02spring.repository;

import ch.usi.inf.bsc.sa4.lab02spring.model.Attempt;
import ch.usi.inf.bsc.sa4.lab02spring.model.AttemptVerificationStatus;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

/// Repository for storing and querying `Attempt` documents.
///
/// In addition to standard MongoDB repository operations, this repository also
/// exposes custom attempt statistics queries.
@Repository
public interface AttemptRepository extends MongoRepository<Attempt, String>, AttemptStatisticsRepository {

    /// Returns all attempts created by the given user.
    /// @param user the user whose attempts should be returned
    /// @return all attempts belonging to the user
    List<Attempt> findByUser(User user);

    /// Returns all completed attempts created by the given user.
    /// @param user the user whose completed attempts should be returned
    /// @return all completed attempts belonging to the user
    List<Attempt> findByUserAndCompletedTrue(User user);

    /// Returns all attempts of the given user for the given level.
    List<Attempt> findByUserAndLevel(User user, Level level);

    /// Returns the attempt with the given id if it belongs to the given user.
    Optional<Attempt> findByIdAndUser(String id, User user);

    /// Deletes all attempts recorded for the given level.
    /// @param level the level whose attempts should be removed
    void deleteByLevel(Level level);

    /// Counts all attempts recorded for the given level.
    /// @param level the level to count attempts for
    /// @return the number of attempts for the level
    long countByLevel(Level level);

    /// Counts all attempts recorded for the given level, excluding its creator.
    /// @param level the level to count attempts for
    /// @param user the creator whose attempts should be excluded
    /// @return the number of non-creator attempts for the level
    long countByLevelAndUserNot(Level level, User user);

    /// Counts completed attempts recorded for the given level.
    /// @param level the level to count completed attempts for
    /// @return the number of completed attempts for the level
    long countByLevelAndCompletedTrue(Level level);

    /// Counts completed attempts recorded for the given level, excluding its creator.
    /// @param level the level to count completed attempts for
    /// @param user the creator whose attempts should be excluded
    /// @return the number of completed non-creator attempts for the level
    long countByLevelAndUserNotAndCompletedTrue(Level level, User user);

    /// Counts attempts recorded for the given level after the provided timestamp.
    /// @param level the level to count attempts for
    /// @param after the lower time bound
    /// @return the number of attempts after the given timestamp
    long countByLevelAndTimestampAfter(Level level, ZonedDateTime after);

    /// Counts attempts recorded for the given level after the provided timestamp,
    /// excluding its creator.
    /// @param level the level to count attempts for
    /// @param user the creator whose attempts should be excluded
    /// @param after the lower time bound
    /// @return the number of non-creator attempts after the given timestamp
    long countByLevelAndUserNotAndTimestampAfter(Level level, User user, ZonedDateTime after);

    /// Counts attempts for the given level, user, status, and lower timestamp
    /// bound, excluding one attempt.
    @Query(value = """
            {
              'level.$id': ?0,
              'user.$id': ?1,
              'antiCheatStatus': ?2,
              'timestamp': { $gt: ?3 },
              '_id': { $ne: ?4 }
            }
            """, count = true)
    long countByLevelUserStatusAndTimestampAfterExcludingAttempt(
            ObjectId levelId,
            String userId,
            AttemptVerificationStatus antiCheatStatus,
            ZonedDateTime after,
            ObjectId id);

    /// Returns another attempt on the same level with the same exact input
    /// fingerprint and similar metadata.
    @Query("""
            {
              'level.$id': ?0,
              'fingerprint.exactHash': ?1,
              '_id': { $ne: ?2 },
              'fingerprint.inputFrameCount': ?3,
              'fingerprint.inputChangeCount': ?4
            }
            """)
    List<Attempt> findExactFingerprintDuplicate(
            ObjectId levelId,
            String exactHash,
            ObjectId id,
            int inputFrameCount,
            int inputChangeCount);

    /// Returns another attempt on the same level with the same
    /// jitter-normalized input fingerprint.
    @Query("""
            {
              'level.$id': ?0,
              'fingerprint.jitterInputHash': ?1,
              '_id': { $ne: ?2 },
              'fingerprint.jitterInputChangeCount': ?3
            }
            """)
    List<Attempt> findJitterFingerprintDuplicate(
            ObjectId levelId,
            String jitterInputHash,
            ObjectId id,
            int jitterInputChangeCount);

    /// Returns another attempt on the same level that shares any fuzzy
    /// input-change fingerprint and similar metadata.
    @Query("""
            {
              'level.$id': ?0,
              'fingerprint.changeBucketHashes': { $in: ?1 },
              '_id': { $ne: ?2 },
              'fingerprint.inputFrameCount': { $gte: ?3, $lte: ?4 },
              'fingerprint.inputChangeCount': { $gte: ?5, $lte: ?6 }
            }
            """)
    List<Attempt> findFuzzyFingerprintDuplicateInMetadataRange(
            ObjectId levelId,
            List<String> changeBucketHashes,
            ObjectId id,
            int minFrameCount,
            int maxFrameCount,
            int minInputChangeCount,
            int maxInputChangeCount);

    /// Returns the fastest completed attempt
    /// for the given level that has a replayable input log.
    /// @param level the level to query
    /// @return the fastest replayable attempt, or empty if none exists
    @Query(value = "{ 'level': ?0, 'completed': true, 'inputLog': { $exists: true, $ne: [] } }",
           sort  = "{ 'timeTaken': 1 }")
    Optional<Attempt> findFastestGhostCandidate(Level level);

    /// Returns the fastest completed attempt
    /// for the given level with a replayable input log
    /// and the given anti-cheat verification status.
    /// @param level  the level to query
    /// @param status the required anti-cheat status
    /// @return the fastest qualifying attempt, or empty if none exists
    @Query(value = "{ 'level': ?0, 'completed': true, 'antiCheatStatus': ?1, 'inputLog': { $exists: true, $ne: [] } }",
           sort  = "{ 'timeTaken': 1 }")
    Optional<Attempt> findFastestVerifiedGhostCandidate(Level level, AttemptVerificationStatus status);

    /// Returns whether the given user
    /// has at least one completed attempt on the given level.
    /// @param user  the user to check
    /// @param level the level to check
    /// @return true when at least one completed attempt exists
    boolean existsByUserAndLevelAndCompletedTrue(User user, Level level);
}
