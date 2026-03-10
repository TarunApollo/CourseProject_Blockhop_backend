package ch.usi.inf.bsc.sa4.lab02spring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.EditorLevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.repository.LevelRepository;
import ch.usi.inf.bsc.sa4.lab02spring.utils.LevelPublishedException;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.Position;
import ch.usi.inf.bsc.sa4.lab02spring.model.GroundObject;
import java.util.HashMap;


@Service
public class EditorService {

    private final LevelRepository levelRepository;

    @Autowired
    public EditorService(LevelRepository levelRepository) {
        this.levelRepository = levelRepository;
    }

    public Level editWorldLayerTile(String userId , String levelId , EditorLevelDTO dto ){
        Level level = levelRepository.findById(levelId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        HashMap<Position,GroundObject> worldLayer = level.getWorldLayer();
        Position targetPosition = dto.position();
        if (!userId.equals(level.getCreatorId())){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        if (targetPosition.x() <= 0 || targetPosition.x() > level.getWidth() || targetPosition.y() <= 0 || targetPosition.x() > level.getHeight()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        if (level.isPublished()){
            throw new LevelPublishedException("Level is already published!");
        }
        if (dto.gid() == 0){
            worldLayer.remove(targetPosition);
        } else {
            worldLayer.put(targetPosition, new GroundObject(dto.gid()));
        }
        return levelRepository.save(level);
    }
}