package ch.usi.inf.bsc.sa4.lab02spring.repository;

/// Repository abstraction for storing and loading level thumbnail images.
public interface ThumbnailRepository {

    /// Stores a thumbnail image for the given level.
    /// @param levelId the id of the level the thumbnail belongs to
    /// @param pngBytes the PNG-encoded thumbnail data
    /// @return the storage id of the saved thumbnail
    String storeThumbnail(String levelId, byte[] pngBytes);

    /// Deletes a thumbnail by its storage id.
    /// @param storageId the storage id of the thumbnail to delete
    void deleteThumbnail(String storageId);

    /// Loads a thumbnail by its storage id.
    /// @param storageId the storage id of the thumbnail to load
    /// @return the thumbnail bytes
    byte[] loadThumbnail(String storageId);
}
