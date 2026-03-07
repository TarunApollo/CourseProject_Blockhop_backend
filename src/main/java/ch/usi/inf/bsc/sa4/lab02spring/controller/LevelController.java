package ch.usi.inf.bsc.sa4.lab02spring.controller;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.UpdateLevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import ch.usi.inf.bsc.sa4.lab02spring.service.EditorService;
import ch.usi.inf.bsc.sa4.lab02spring.service.LevelService;

@RestController
@RequestMapping("/levels")
public class LevelController {
  private final LevelService levelService;
  private final EditorService editorService;

  @Autowired
  public LevelController(LevelService levelService, EditorService editorService) {
    this.levelService = levelService;
    this.editorService = editorService;
  }

  // TODO: add all GET methods here like get published levels and whatnot..

  @PutMapping("/{id}/properties")
  public ResponseEntity<Level> updateLevel(@PathVariable String id, @RequestBody UpdateLevelDTO dto) {
    return ResponseEntity.of(this.levelService.updateLevelProperties(id, dto));
  }
}
