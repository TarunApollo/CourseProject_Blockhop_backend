package ch.usi.inf.bsc.sa4.lab02spring.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.CloneLevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.CreateLevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.LevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.service.EditorService;

@RestController
@RequestMapping("/users")
public class EditorController {
    

    private final EditorService editorService;
    @Autowired
        public EditorController(EditorService editorService) {
        this.editorService = editorService;
    }

    @PostMapping("/{userId}/levels")
    public LevelDTO createLevel(
            @PathVariable String userId,
            @RequestBody CreateLevelDTO createLevelDTO) {
        return new LevelDTO(editorService.createLevel(userId, createLevelDTO));
    }

    @PostMapping("/{userId}/levels/clone")
    public ResponseEntity<LevelDTO> cloneLevel(
            @PathVariable String userId,
            @RequestBody CloneLevelDTO cloneLevelDTO) {
        return ResponseEntity.of(
            editorService.cloneLevel(userId, cloneLevelDTO.sourceLevelId()).map(LevelDTO::new)
        );
    }

}
