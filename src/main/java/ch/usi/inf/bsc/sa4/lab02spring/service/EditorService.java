package ch.usi.inf.bsc.sa4.lab02spring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ch.usi.inf.bsc.sa4.lab02spring.repository.LevelRepository;


@Service
public class EditorService {

    private final LevelRepository levelRepository;

    ///
    /// Creates a new EditorService with the given repository.
    /// @spec.requires levelRepository is not null.
    /// @spec.effects creates an EditorService with the given levelRepository.
    /// @param levelRepository the repository used to store levels.
    @Autowired
    public EditorService(LevelRepository levelRepository) {
        this.levelRepository = levelRepository;
    }
}