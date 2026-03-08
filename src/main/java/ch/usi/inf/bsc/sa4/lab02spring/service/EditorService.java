package ch.usi.inf.bsc.sa4.lab02spring.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.repository.LevelRepository;


@Service
public class EditorService {

    private final LevelRepository levelRepository;

    @Autowired
    public EditorService(LevelRepository levelRepository) {
        this.levelRepository = levelRepository;
    }

    public Optional<Level> cloneLevel(String creatorId, String sourceLevelId) {
        Optional<Level> sourceOpt = levelRepository.findById(sourceLevelId);
        return sourceOpt.flatMap(source -> {
            boolean isOwner = source.getCreatorId().equals(creatorId);
            boolean isPublished = source.isPublished();
            if (!isOwner && !isPublished) {
                return Optional.empty();
            }
            Level cloned = source.cloneFor(creatorId);
            return Optional.of(levelRepository.save(cloned));
        });
    }
}