package ch.usi.inf.bsc.sa4.lab02spring.service;

import ch.usi.inf.bsc.sa4.lab02spring.model.Box;
import ch.usi.inf.bsc.sa4.lab02spring.model.Coin;
import ch.usi.inf.bsc.sa4.lab02spring.model.Content;
import ch.usi.inf.bsc.sa4.lab02spring.model.CoinType;
import ch.usi.inf.bsc.sa4.lab02spring.model.Decoration;
import ch.usi.inf.bsc.sa4.lab02spring.model.ExitDoor;
import ch.usi.inf.bsc.sa4.lab02spring.model.GameObject;
import ch.usi.inf.bsc.sa4.lab02spring.model.Position;
import ch.usi.inf.bsc.sa4.lab02spring.model.Shell;
import ch.usi.inf.bsc.sa4.lab02spring.model.Slime;
import ch.usi.inf.bsc.sa4.lab02spring.model.Snail;
import ch.usi.inf.bsc.sa4.lab02spring.model.StartFlag;
import org.springframework.stereotype.Component;

@Component
public class GameObjectFactory {

    private final TileSetService tileSetService;

    public GameObjectFactory(TileSetService tileSetService) {
        this.tileSetService = tileSetService;
    }

    public GameObject createGameObject(int gid, Position pos) {
        return createGameObject(gid, pos, new Content.NoContent());
    }

    public GameObject createGameObject(int gid, Position pos, Content content) {
        String type = tileSetService.getObjectTileType(gid);
        
        return switch (type) {
            case "Decoration", "ExclamationMark" -> new Decoration(gid, pos);
            case "Enemy_Slime_Normal" -> new Slime(gid, pos);
            case "Enemy_Snail" -> new Snail(gid, pos);
            case "Box", "BoxDouble" -> new Box(gid, pos, content);
            case "Start_Flag", "Start_Flag_B" -> new StartFlag(gid, pos);
            case "Door_Closed", "Door_Open" -> new ExitDoor(gid, pos);
            case "Item_Coin_Gold", "Item_Coin_Gold_Side",
                 "Item_Coin_Silver", "Item_Coin_Silver_Side",
                 "Item_Coin_Bronze", "Item_Coin_Bronze_Side" -> createCoin(gid, pos, type);
            case "Item_Shell" -> new Shell(gid, pos);
            default -> throw new IllegalArgumentException("Unknown object type: " + type);
        };
    }

    private Coin createCoin(int gid, Position pos, String type) {
        String baseType = type.endsWith("_Side") 
            ? type.substring(0, type.length() - 5) 
            : type;
        return new Coin(gid, pos, CoinType.fromValue(baseType));
    }
}
