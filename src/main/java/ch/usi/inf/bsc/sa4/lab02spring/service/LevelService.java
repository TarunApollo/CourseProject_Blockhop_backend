package ch.usi.inf.bsc.sa4.lab02spring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.usi.inf.bsc.sa4.lab02spring.repository.LevelRepository;

@Service
public class LevelService {
    
    private final LevelRepository levelRepository;
    private final UserService userService;

    @Autowired
    public LevelService(LevelRepository levelRepository, UserService userService) {
        this.levelRepository = levelRepository;
        this.userService = userService;
    }
}
