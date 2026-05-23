package ch.usi.inf.bsc.sa4.lab02spring.service;

import ch.usi.inf.bsc.sa4.lab02spring.model.Box;
import ch.usi.inf.bsc.sa4.lab02spring.model.Bee;
import ch.usi.inf.bsc.sa4.lab02spring.model.Coin;
import ch.usi.inf.bsc.sa4.lab02spring.model.CoinType;
import ch.usi.inf.bsc.sa4.lab02spring.model.Content;
import ch.usi.inf.bsc.sa4.lab02spring.model.Decoration;
import ch.usi.inf.bsc.sa4.lab02spring.model.ExitDoor;
import ch.usi.inf.bsc.sa4.lab02spring.model.GameObject;
import ch.usi.inf.bsc.sa4.lab02spring.model.Position;
import ch.usi.inf.bsc.sa4.lab02spring.model.Slime;
import ch.usi.inf.bsc.sa4.lab02spring.model.Snail;
import ch.usi.inf.bsc.sa4.lab02spring.model.SpikedAlien;
import ch.usi.inf.bsc.sa4.lab02spring.model.StartFlag;
import ch.usi.inf.bsc.sa4.lab02spring.utils.UnknownObjectTypeException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.stream.Stream;

/// Service tests for [GameObjectFactory].
@SpringBootTest
@DisplayName("The GameObjectFactory Service")
class GameObjectFactoryTests {

    /// Tile gid used by fixtures.
    private static final int GID = 42;

    /// Position used by fixtures.
    private static final Position POS = new Position(1, 2);

    /// Tile type string for box objects.
    private static final String TYPE_BOX = "Box";

    /// Tile type string for double box objects.
    private static final String TYPE_BOX_DOUBLE = "BoxDouble";

    /// Tile type string for decoration objects.
    private static final String TYPE_DECORATION = "Decoration";

    /// Tile type string for slime enemy objects.
    private static final String TYPE_ENEMY_SLIME = "Enemy_Slime_Normal";

    /// Tile type string for snail enemy objects.
    private static final String TYPE_ENEMY_SNAIL = "Enemy_Snail";

    /// Tile type string for bee enemy objects.
    private static final String TYPE_ENEMY_BEE = "Enemy_Bee";

    /// Tile type string for spiked alien enemy objects.
    private static final String TYPE_ENEMY_SPIKED_ALIEN = "Enemy_Spiked_Alien";

    /// Tile type string for start flag objects.
    private static final String TYPE_START_FLAG = "Start_Flag";

    /// Tile type string for door objects.
    private static final String TYPE_DOOR_CLOSED = "Door_Closed";

    /// Tile type string for gold coin objects.
    private static final String TYPE_GOLD = "Item_Coin_Gold";

    /// Tile type string for silver coin objects.
    private static final String TYPE_SILVER = "Item_Coin_Silver";

    /// Tile type string for bronze coin objects.
    private static final String TYPE_BRONZE = "Item_Coin_Bronze";

    /// Unknown tile type used for failure-path tests.
    private static final String TYPE_UNKNOWN = "Not_A_Real_Type";

    /// Factory under test.
    @Autowired
    private GameObjectFactory gameObjectFactory;

    /// Mocked tileset service.
    @MockitoBean
    private TileSetService tileSetService;

    /// Provides tile type strings and their expected [GameObject] subtypes for
    /// dispatch tests.
    /* package */ static Stream<Arguments> dispatchCases() {
        return Stream.of(
                Arguments.of(TYPE_DECORATION, Decoration.class),
                Arguments.of(TYPE_ENEMY_SLIME, Slime.class),
                Arguments.of(TYPE_ENEMY_SNAIL, Snail.class),
                Arguments.of(TYPE_BOX, Box.class),
                Arguments.of(TYPE_BOX_DOUBLE, Box.class),
                Arguments.of(TYPE_START_FLAG, StartFlag.class),
                Arguments.of(TYPE_DOOR_CLOSED, ExitDoor.class),
                Arguments.of(TYPE_GOLD, Coin.class),
                Arguments.of(TYPE_SILVER, Coin.class),
                Arguments.of(TYPE_BRONZE, Coin.class),
                Arguments.of(TYPE_ENEMY_BEE, Bee.class),
                Arguments.of(TYPE_ENEMY_SPIKED_ALIEN, SpikedAlien.class));
    }

    /// Tests for `createGameObject`.
    @Nested
    @DisplayName("when creating game objects")
    class CreateGameObject {

        /// The factory must dispatch each tile type to the correct subtype.
        @ParameterizedTest(name = "{0} -> {1}")
        @MethodSource("ch.usi.inf.bsc.sa4.lab02spring.service.GameObjectFactoryTests#dispatchCases")
        @DisplayName("dispatches each tile type to the correct subtype")
        void dispatchesToCorrectSubtype(final String tileType, final Class<? extends GameObject> expected) {
            Mockito.when(tileSetService.getObjectTileType(GID)).thenReturn(tileType);
            final GameObject result = gameObjectFactory.createGameObject(GID, POS);

            Assertions.assertInstanceOf(expected, result);

            Mockito.verify(tileSetService).getObjectTileType(GID);
        }

        /// The factory must propagate gid and pos into every constructed object.
        @ParameterizedTest(name = "{0}")
        @MethodSource("ch.usi.inf.bsc.sa4.lab02spring.service.GameObjectFactoryTests#dispatchCases")
        @DisplayName("propagates gid and pos into every constructed object")
        void propagatesGidAndPos(final String tileType) {
            Mockito.when(tileSetService.getObjectTileType(GID)).thenReturn(tileType);
            final GameObject result = gameObjectFactory.createGameObject(GID, POS);

            Assertions.assertEquals(GID, result.gid());
            Assertions.assertEquals(POS, result.pos());

            Mockito.verify(tileSetService).getObjectTileType(GID);
        }

        /// Box objects must preserve caller-provided content.
        @Test
        @DisplayName("propagates content into box objects")
        void propagatesBoxContent() {
            Mockito.when(tileSetService.getObjectTileType(GID)).thenReturn(TYPE_BOX);
            final Content content = new Content.SomeContent(CoinType.BRONZE_COIN);
            final Box box = (Box) gameObjectFactory.createGameObject(GID, POS, content);

            Assertions.assertEquals(content, box.content());

            Mockito.verify(tileSetService).getObjectTileType(GID);
        }

        /// The 2-argument overload must default to NoContent.
        @Test
        @DisplayName("defaults content to NoContent in the 2-argument overload")
        void defaultsToNoContent() {
            Mockito.when(tileSetService.getObjectTileType(GID)).thenReturn(TYPE_BOX);
            final Box box = (Box) gameObjectFactory.createGameObject(GID, POS);

            Assertions.assertInstanceOf(Content.NoContent.class, box.content());

            Mockito.verify(tileSetService).getObjectTileType(GID);
        }

        /// Coin tiles must produce coins with the expected [CoinType].
        @ParameterizedTest(name = "{0} -> {1}")
        @CsvSource({
            "Item_Coin_Gold,GOLD_COIN",
            "Item_Coin_Silver,SILVER_COIN",
            "Item_Coin_Bronze,BRONZE_COIN"
        })
        @DisplayName("creates coin objects with the expected type")
        void createsCoinsWithExpectedType(final String tileType, final CoinType expectedType) {
            Mockito.when(tileSetService.getObjectTileType(GID)).thenReturn(tileType);
            final Coin coin = (Coin) gameObjectFactory.createGameObject(GID, POS);

            Assertions.assertEquals(expectedType, coin.type());

            Mockito.verify(tileSetService).getObjectTileType(GID);
        }

        /// Unknown tile types must surface as exceptions.
        @Test
        @DisplayName("throws UnknownObjectTypeException for unknown tile types")
        void rejectsUnknownTileTypes() {
            Mockito.when(tileSetService.getObjectTileType(GID)).thenReturn(TYPE_UNKNOWN);

            Assertions.assertThrows(UnknownObjectTypeException.class,
                    () -> gameObjectFactory.createGameObject(GID, POS));

            Mockito.verify(tileSetService).getObjectTileType(GID);
        }
    }
}
