package ch.usi.inf.bsc.sa4.lab02spring.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.usi.inf.bsc.sa4.lab02spring.service.EditorService;

@RestController
@RequestMapping("/users")
public class EditorController {
    

    private final EditorService editorService;
    @Autowired
        public EditorController(EditorService editorService) {
        this.editorService = editorService;
    }


}
