package ch.usi.inf.bsc.sa4.lab02spring.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.jspecify.annotations.Nullable;


@Document(collection = "levelThumbnails")
public record LevelThumbnail(
    @Id @Nullable String id,
    String levelId,
    String storageId
) {}
