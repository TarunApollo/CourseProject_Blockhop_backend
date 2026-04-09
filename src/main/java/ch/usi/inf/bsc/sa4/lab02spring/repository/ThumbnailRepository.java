package ch.usi.inf.bsc.sa4.lab02spring.repository;

public interface ThumbnailRepository {
    String storeThumbnail(String levelId, byte[] pngBytes);
    void deleteThumbnail(String storageId);
}
