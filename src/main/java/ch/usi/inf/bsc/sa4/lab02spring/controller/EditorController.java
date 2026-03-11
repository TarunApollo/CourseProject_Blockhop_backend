package ch.usi.inf.bsc.sa4.lab02spring.controller;

import ch.usi.inf.bsc.sa4.lab02spring.repository.LevelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ch.usi.inf.bsc.sa4.lab02spring.service.EditorService;

@RestController
@RequestMapping("/editor")
public class EditorController {
    private final LevelRepository levelRepository;
    private final EditorService editorService;

    @Autowired
    public EditorController(EditorService editorService, LevelRepository levelRepository) {
        this.levelRepository = levelRepository;
        this.editorService = editorService;
    }
}
