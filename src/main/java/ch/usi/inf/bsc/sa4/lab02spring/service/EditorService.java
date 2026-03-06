package ch.usi.inf.bsc.sa4.lab02spring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.usi.inf.bsc.sa4.lab02spring.repository.LevelRepository;
import ch.usi.inf.bsc.sa4.lab02spring.repository.UserRepository;

@Service
public class EditorService {
    
    private final LevelRepository levelRepository;

    @Autowired
    public EditorService(LevelRepository levelRepository) {
        this.levelRepository = levelRepository;
    }
}
