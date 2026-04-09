package ch.usi.inf.bsc.sa4.lab02spring.repository;

import java.io.ByteArrayInputStream;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ThumbnailRepositoryImpl implements ThumbnailRepository {
    private final GridFsTemplate gridFsTemplate;

    public ThumbnailRepositoryImpl(GridFsTemplate gridFsTemplate) {
        this.gridFsTemplate = gridFsTemplate;
    }

    @Override
    public String storeThumbnail(String levelId, byte[] pngBytes) {
        ObjectId fileId = gridFsTemplate.store(
                new ByteArrayInputStream(pngBytes),
                "level-" + levelId + ".png",
                "image/png",
                new Document("levelId", levelId));
        return fileId.toHexString();
    }

    @Override
    public void deleteThumbnail(String storageId) {
        gridFsTemplate.delete(
                Query.query(Criteria.where("_id").is(new ObjectId(storageId))));
    }
}
