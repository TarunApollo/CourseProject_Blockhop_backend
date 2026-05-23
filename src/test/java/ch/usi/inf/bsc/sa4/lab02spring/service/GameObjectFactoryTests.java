package ch.usi.inf.bsc.sa4.lab02spring.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import ch.usi.inf.bsc.sa4.lab02spring.model.Box;
import ch.usi.inf.bsc.sa4.lab02spring.model.Coin;
import ch.usi.inf.bsc.sa4.lab02spring.model.CoinType;
import ch.usi.inf.bsc.sa4.lab02spring.model.Content;
import ch.usi.inf.bsc.sa4.lab02spring.model.Decoration;
import ch.usi.inf.bsc.sa4.lab02spring.model.GameObject;
import ch.usi.inf.bsc.sa4.lab02spring.model.Position;
import ch.usi.inf.bsc.sa4.lab02spring.model.Slime;
import ch.usi.inf.bsc.sa4.lab02spring.utils.UnknownObjectTypeException;

/// Tests for [GameObjectFactory].
@SuppressWarnings("PMD.TooManyStaticImports")
@SpringBootTest
@DisplayName("Game Object Factory")
class GameObjectFactoryTests {

    /// Tile ID for decoration objects.
    private static final String TILE_DECO = "tile.deco";
    /// Tile ID for slime enemies.
    private static final String TILE_SLIME = "tile.slime";
    /// Tile ID for box objects.
    private static final String TILE_BOX = "tile.box";
    /// Expected object type for boxes.
    private static final String TYPE_BOX = "Box";
    /// Tile ID for gold coin objects.
    private static final String TILE_COIN_GOLD = "tile.coin.gold";
    /// Tile ID used to test unknown types.
    private static final String TILE_UNKNOWN = "tile.unknown";

    /// Mock tile catalog used by the factory.
    @MockitoBean
    private TileCatalogService tileCatalogService;

    /// Factory under test.
    private GameObjectFactory factory;

    @BeforeEach
    void setup() {
        this.factory = new GameObjectFactory(tileCatalogService);
    }

    /// Tests creation of decoration object.
    @Test
    @DisplayName("creates Decoration from tile type")
    void createsDecoration() {
        Mockito.when(tileCatalogService.getType(TILE_DECO)).thenReturn("Decoration");
        final GameObject obj = factory.createGameObject(TILE_DECO, new Position(1, 2));
        
        assertInstanceOf(Decoration.class, obj);
        assertEquals(TILE_DECO, obj.tileId());
        assertEquals(new Position(1, 2), obj.pos());
    }

    /// Tests creation of slime enemy.
    @Test
    @DisplayName("creates Slime from tile type")
    void createsSlime() {
        Mockito.when(tileCatalogService.getType(TILE_SLIME)).thenReturn("Enemy_Slime_Normal");
        final GameObject obj = factory.createGameObject(TILE_SLIME, new Position(2, 3));
        
        assertInstanceOf(Slime.class, obj);
        assertEquals(TILE_SLIME, obj.tileId());
    }

    /// Tests creation of a box with default content.
    @Test
    @DisplayName("creates Box with no content")
    void createsBoxWithoutContent() {
        Mockito.when(tileCatalogService.getType(TILE_BOX)).thenReturn(TYPE_BOX);
        final GameObject obj = factory.createGameObject(TILE_BOX, new Position(1, 1));
        
        final Box box = assertInstanceOf(Box.class, obj);
        assertTrue(box.content() instanceof Content.NoContent);
    }

    /// Tests creation of a box with content.
    @Test
    @DisplayName("creates Box with content")
    void createsBoxWithContent() {
        Mockito.when(tileCatalogService.getType(TILE_BOX)).thenReturn(TYPE_BOX);
        final Content content = new Content.SomeContent(CoinType.GOLD_COIN);
        final GameObject obj = factory.createGameObject(TILE_BOX, new Position(1, 1), content);
        
        final Box box = assertInstanceOf(Box.class, obj);
        assertEquals(content, box.content());
    }

    /// Tests creation of a coin.
    @Test
    @DisplayName("creates Gold Coin from tile type")
    void createsGoldCoin() {
        Mockito.when(tileCatalogService.getType(TILE_COIN_GOLD)).thenReturn("Item_Coin_Gold");
        final GameObject obj = factory.createGameObject(TILE_COIN_GOLD, new Position(0, 0));
        
        final Coin coin = assertInstanceOf(Coin.class, obj);
        assertEquals(CoinType.GOLD_COIN, coin.type());
    }

    /// Fails for unknown object types.
    @Test
    @DisplayName("fails on unknown object type")
    void failsOnUnknownType() {
        Mockito.when(tileCatalogService.getType(TILE_UNKNOWN)).thenReturn("UnknownType");
        
        assertThrows(UnknownObjectTypeException.class, 
                () -> factory.createGameObject(TILE_UNKNOWN, new Position(0, 0)));
    }
}
