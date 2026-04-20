package ch.usi.inf.bsc.sa4.lab02spring.repository;

import java.io.ByteArrayInputStream;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Repository;

import java.io.IOException;

import com.mongodb.client.gridfs.model.GridFSFile;
import org.springframework.data.mongodb.gridfs.GridFsResource;

/// GridFS-backed implementation of {@link ThumbnailRepository}.
@Repository
public class ThumbnailRepositoryImpl implements ThumbnailRepository {

    /// GridFS template used to store, load, and delete thumbnail files.
    private final GridFsTemplate gridFsTemplate;

    /// Creates a thumbnail repository backed by the given GridFS template.
    /// @param gridFsTemplate the template used for thumbnail storage
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

    @Override
    public byte[] loadThumbnail(String storageId)
    {
        GridFSFile file = gridFsTemplate.findOne(
             Query.query(Criteria.where("_id").is(new ObjectId(storageId))));
        
        if (file == null)
        {
            throw new IllegalArgumentException("Thumbnail not found");
        }

        GridFsResource resource = gridFsTemplate.getResource(file);

        try
        {
            return resource.getInputStream().readAllBytes();
        }
        catch(IOException e)
        {
            throw new IllegalStateException("Failed to read thumbnail");
        }

    }
}
