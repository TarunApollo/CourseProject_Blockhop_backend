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
import ch.usi.inf.bsc.sa4.lab02spring.model.Content;
import ch.usi.inf.bsc.sa4.lab02spring.model.Decoration;
import ch.usi.inf.bsc.sa4.lab02spring.model.GameObject;
import ch.usi.inf.bsc.sa4.lab02spring.model.Position;
import ch.usi.inf.bsc.sa4.lab02spring.model.Slime;
import ch.usi.inf.bsc.sa4.lab02spring.utils.UnknownObjectTypeException;

/// Tests for [GameObjectFactory].
@SpringBootTest
@DisplayName("Game Object Factory")
class GameObjectFactoryTests {

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
        Mockito.when(tileCatalogService.getType("tile.deco")).thenReturn("Decoration");
        final GameObject obj = factory.createGameObject("tile.deco", new Position(1, 2));
        
        assertInstanceOf(Decoration.class, obj);
        assertEquals("tile.deco", obj.tileId());
        assertEquals(new Position(1, 2), obj.pos());
    }

    /// Tests creation of slime enemy.
    @Test
    @DisplayName("creates Slime from tile type")
    void createsSlime() {
        Mockito.when(tileCatalogService.getType("tile.slime")).thenReturn("Enemy_Slime_Normal");
        final GameObject obj = factory.createGameObject("tile.slime", new Position(2, 3));
        
        assertInstanceOf(Slime.class, obj);
        assertEquals("tile.slime", obj.tileId());
    }

    /// Tests creation of a box with default content.
    @Test
    @DisplayName("creates Box with no content")
    void createsBoxWithoutContent() {
        Mockito.when(tileCatalogService.getType("tile.box")).thenReturn("Box");
        final GameObject obj = factory.createGameObject("tile.box", new Position(1, 1));
        
        final Box box = assertInstanceOf(Box.class, obj);
        assertTrue(box.content() instanceof Content.NoContent);
    }

    /// Tests creation of a box with content.
    @Test
    @DisplayName("creates Box with content")
    void createsBoxWithContent() {
        Mockito.when(tileCatalogService.getType("tile.box")).thenReturn("Box");
        final Content content = new Content.SomeContent(ch.usi.inf.bsc.sa4.lab02spring.model.CoinType.GOLD_COIN);
        final GameObject obj = factory.createGameObject("tile.box", new Position(1, 1), content);
        
        final Box box = assertInstanceOf(Box.class, obj);
        assertEquals(content, box.content());
    }

    /// Tests creation of a coin.
    @Test
    @DisplayName("creates Gold Coin from tile type")
    void createsGoldCoin() {
        Mockito.when(tileCatalogService.getType("tile.coin.gold")).thenReturn("Item_Coin_Gold");
        final GameObject obj = factory.createGameObject("tile.coin.gold", new Position(0, 0));
        
        final Coin coin = assertInstanceOf(Coin.class, obj);
        assertEquals(ch.usi.inf.bsc.sa4.lab02spring.model.CoinType.GOLD_COIN, coin.type());
    }

    /// Fails for unknown object types.
    @Test
    @DisplayName("fails on unknown object type")
    void failsOnUnknownType() {
        Mockito.when(tileCatalogService.getType("tile.unknown")).thenReturn("UnknownType");
        
        assertThrows(UnknownObjectTypeException.class, 
                () -> factory.createGameObject("tile.unknown", new Position(0, 0)));
    }
}
