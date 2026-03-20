package ch.usi.inf.bsc.sa4.lab02spring.service;

import ch.usi.inf.bsc.sa4.lab02spring.model.Box;
import ch.usi.inf.bsc.sa4.lab02spring.model.Coin;
import ch.usi.inf.bsc.sa4.lab02spring.model.Content;
import ch.usi.inf.bsc.sa4.lab02spring.model.Decoration;
import ch.usi.inf.bsc.sa4.lab02spring.model.ExitDoor;
import ch.usi.inf.bsc.sa4.lab02spring.model.GameObject;
import ch.usi.inf.bsc.sa4.lab02spring.model.GameObjectTileId;
import ch.usi.inf.bsc.sa4.lab02spring.model.Position;
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

    public GameObject createGameObject(GameObjectTileId tileId, Position pos) {
        String type = tileSetService.getObjectTileType(tileId.value());

        return switch (type) {
            case "Decoration", "ExclamationMark" -> new Decoration(pos);
            case "Enemy_Slime_Normal" -> new Slime(pos);
            case "Enemy_Snail" -> new Snail(pos, false);
            case "Box", "BoxDouble" -> new Box(pos, new Content.NoContent());
            case "Start_Flag", "Start_Flag_B" -> new StartFlag(pos);
            case "Door_Closed", "Door_Open" -> new ExitDoor(pos);
            case "Item_Coin_Gold", "Item_Coin_Gold_Side" -> new Coin(pos, 100);
            case "Item_Coin_Silver", "Item_Coin_Silver_Side" -> new Coin(pos, 25);
            case "Item_Coin_Bronze", "Item_Coin_Bronze_Side" -> new Coin(pos, 5);
            case "Item_Shell" -> new Snail(pos, true);
            default -> throw new IllegalArgumentException("Unknown object type: " + type);
        };
    }
}
