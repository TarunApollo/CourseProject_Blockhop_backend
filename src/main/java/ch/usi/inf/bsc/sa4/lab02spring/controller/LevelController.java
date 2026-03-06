package ch.usi.inf.bsc.sa4.lab02spring.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
