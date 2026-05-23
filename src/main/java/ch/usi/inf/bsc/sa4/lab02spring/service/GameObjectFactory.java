package ch.usi.inf.bsc.sa4.lab02spring.service;

import ch.usi.inf.bsc.sa4.lab02spring.model.Box;
import ch.usi.inf.bsc.sa4.lab02spring.model.Bee;
import ch.usi.inf.bsc.sa4.lab02spring.model.Coin;
import ch.usi.inf.bsc.sa4.lab02spring.model.Content;
import ch.usi.inf.bsc.sa4.lab02spring.model.CoinType;
import ch.usi.inf.bsc.sa4.lab02spring.model.Decoration;
import ch.usi.inf.bsc.sa4.lab02spring.model.ExitDoor;
import ch.usi.inf.bsc.sa4.lab02spring.model.GameObject;
import ch.usi.inf.bsc.sa4.lab02spring.model.Position;
import ch.usi.inf.bsc.sa4.lab02spring.model.Slime;
import ch.usi.inf.bsc.sa4.lab02spring.model.Snail;
import ch.usi.inf.bsc.sa4.lab02spring.model.StartFlag;
import ch.usi.inf.bsc.sa4.lab02spring.utils.UnknownObjectTypeException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.springframework.stereotype.Component;

/// Builds GameObject instances from tile types resolved by TileCatalogService.
@Component
public class GameObjectFactory {

    /// Resolves a catalog tile id to its type string.
    private final TileCatalogService tileCatalogService;

    /// Constructs a factory backed by the given tileset service.
    /// 
    /// @param tileCatalogService the service used to map tile ids to type strings
    public GameObjectFactory(final TileCatalogService tileCatalogService) {
        this.tileCatalogService = tileCatalogService;
    }

    /// Builds a GameObject without content (defaults to NoContent).
    /// 
    /// @param tileId the catalog tile id to instantiate
    /// @param pos    the position of the new game object
    /// @return the constructed GameObject
    public GameObject createGameObject(final String tileId, final Position pos) {
        return createGameObject(tileId, pos, new Content.NoContent());
    }

    /// Builds a GameObject for the given tile id, dispatching on the resolved tile
    /// type.
    /// 
    /// @param tileId  the catalog tile id to instantiate
    /// @param pos     the position of the new game object
    /// @param content the optional content carried by the new game object (used by
    ///                Box)
    /// @return the constructed GameObject
    /// @throws UnknownObjectTypeException if the tile type is not registered
    public GameObject createGameObject(final String tileId, final Position pos, final Content content) {
        final String type = tileCatalogService.getType(tileId);
        return ObjectTypeEnum.fromValue(type).createGameObject(tileId, pos, content);
    }

    /// Maps each tile-type string to a constructor for the corresponding
    /// GameObject. New tile types must be registered here so the factory can
    /// dispatch them.
    /* package */ enum ObjectTypeEnum {

        DECORATION("Decoration") {
            @Override
            public GameObject createGameObject(final String tileId, final Position pos, final Content content) {
                return new Decoration(tileId, pos);
            }
        },

        ENEMY_SLIME_NORMAL("Enemy_Slime_Normal") {
            @Override
            public GameObject createGameObject(final String tileId, final Position pos, final Content content) {
                return new Slime(tileId, pos);
            }
        },

        ENEMY_SNAIL("Enemy_Snail") {
            @Override
            public GameObject createGameObject(final String tileId, final Position pos, final Content content) {
                return new Snail(tileId, pos);
            }
        },

        ENEMY_BEE("Enemy_Bee") {
            @Override
            public GameObject createGameObject(final String tileId, final Position pos, final Content content) {
                return new Bee(tileId, pos);
            }
        },

        BOX("Box") {
            @Override
            public GameObject createGameObject(final String tileId, final Position pos, final Content content) {
                return new Box(tileId, pos, content);
            }
        },

        BOX_DOUBLE("BoxDouble") {
            @Override
            public GameObject createGameObject(final String tileId, final Position pos, final Content content) {
                return new Box(tileId, pos, content);
            }
        },

        START_FLAG("Start_Flag") {
            @Override
            public GameObject createGameObject(final String tileId, final Position pos, final Content content) {
                return new StartFlag(tileId, pos);
            }
        },

        DOOR_CLOSED("Door_Closed") {
            @Override
            public GameObject createGameObject(final String tileId, final Position pos, final Content content) {
                return new ExitDoor(tileId, pos);
            }
        },

        ITEM_COIN_GOLD("Item_Coin_Gold") {
            @Override
            public GameObject createGameObject(final String tileId, final Position pos, final Content content) {
                return new Coin(tileId, pos, CoinType.GOLD_COIN);
            }
        },

        ITEM_COIN_SILVER("Item_Coin_Silver") {
            @Override
            public GameObject createGameObject(final String tileId, final Position pos, final Content content) {
                return new Coin(tileId, pos, CoinType.SILVER_COIN);
            }
        },

        ITEM_COIN_BRONZE("Item_Coin_Bronze") {
            @Override
            public GameObject createGameObject(final String tileId, final Position pos, final Content content) {
                return new Coin(tileId, pos, CoinType.BRONZE_COIN);
            }
        };

        /// JSON tile-type string carried by this enum constant.
        private final String jsonValue;

        ObjectTypeEnum(final String jsonValue) {
            this.jsonValue = jsonValue;
        }

        /// JSON value used to serialize this enum (matches the editor's tile
        /// type string).
        @JsonValue
        public String value() {
            return jsonValue;
        }

        /// Constructs the GameObject instance corresponding to this tile type.
        /// 
        /// @param tileId  the catalog tile id
        /// @param pos     the position of the new game object
        /// @param content the content carried by the new game object (used by Box)
        /// @return the constructed GameObject
        public abstract GameObject createGameObject(String tileId, Position pos, Content content);

        /// Resolves the enum constant for the given JSON tile-type string.
        /// 
        /// @param value the JSON tile-type string
        /// @return the corresponding enum constant
        /// @throws UnknownObjectTypeException if no enum constant matches the value
        @JsonCreator
        public static ObjectTypeEnum fromValue(final String value) {
            for (final ObjectTypeEnum type : values()) {
                if (type.jsonValue.equals(value)) {
                    return type;
                }
            }
            throw new UnknownObjectTypeException();
        }
    }
}
