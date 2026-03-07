package ch.usi.inf.bsc.sa4.lab02spring.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.CreateLevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.repository.LevelRepository;
import ch.usi.inf.bsc.sa4.lab02spring.repository.UserRepository;

@Service
public class EditorService {

    private final LevelRepository levelRepository;

    @Autowired
    public EditorService(LevelRepository levelRepository) {
        this.levelRepository = levelRepository;
    }

    public Level createLevel(String creatorId, CreateLevelDTO createLevelDTO) {
        Level level = new Level(
            createLevelDTO.title(),
            createLevelDTO.description(),
            creatorId
        );
        return levelRepository.save(level);
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