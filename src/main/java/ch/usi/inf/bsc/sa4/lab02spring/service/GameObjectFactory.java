package ch.usi.inf.bsc.sa4.lab02spring.service;

import ch.usi.inf.bsc.sa4.lab02spring.model.Box;
import ch.usi.inf.bsc.sa4.lab02spring.model.Coin;
import ch.usi.inf.bsc.sa4.lab02spring.model.Content;
import ch.usi.inf.bsc.sa4.lab02spring.model.Decoration;
import ch.usi.inf.bsc.sa4.lab02spring.model.ExitDoor;
import ch.usi.inf.bsc.sa4.lab02spring.model.GameObject;
import ch.usi.inf.bsc.sa4.lab02spring.model.Position;
import ch.usi.inf.bsc.sa4.lab02spring.model.Slime;
import ch.usi.inf.bsc.sa4.lab02spring.model.Snail;
import ch.usi.inf.bsc.sa4.lab02spring.model.StartFlag;
import ch.usi.inf.bsc.sa4.lab02spring.utils.UnknownObjectTypeException;

import org.springframework.stereotype.Component;

@Component
public class GameObjectFactory {

    private final TileSetService tileSetService;

    public GameObjectFactory(TileSetService tileSetService) {
        this.tileSetService = tileSetService;
    }

    public GameObject createGameObject(int gid, Position pos) {
        String type = tileSetService.getObjectTileType(gid);
        
        return switch (type) {
            case "Decoration", "ExclamationMark" -> new Decoration(gid, pos);
            case "Enemy_Slime_Normal" -> new Slime(gid, pos);
            case "Enemy_Snail" -> new Snail(gid, pos, false);
            case "Box", "BoxDouble" -> new Box(gid, pos, new Content.NoContent());
            case "Start_Flag", "Start_Flag_B" -> new StartFlag(gid, pos);
            case "Door_Closed", "Door_Open" -> new ExitDoor(gid, pos);
            case "Item_Coin_Gold", "Item_Coin_Gold_Side" -> new Coin(gid, pos, 100);
            case "Item_Coin_Silver", "Item_Coin_Silver_Side" -> new Coin(gid, pos, 25);
            case "Item_Coin_Bronze", "Item_Coin_Bronze_Side" -> new Coin(gid, pos, 5);
            case "Item_Shell" -> new Snail(gid, pos, true);
            default -> throw new UnknownObjectTypeException();
        };
    }
}
