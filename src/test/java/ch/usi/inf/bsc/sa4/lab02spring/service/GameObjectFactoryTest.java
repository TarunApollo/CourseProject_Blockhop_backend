package ch.usi.inf.bsc.sa4.lab02spring.service;

import ch.usi.inf.bsc.sa4.lab02spring.model.Box;
import ch.usi.inf.bsc.sa4.lab02spring.model.Coin;
import ch.usi.inf.bsc.sa4.lab02spring.model.CoinType;
import ch.usi.inf.bsc.sa4.lab02spring.model.Content;
import ch.usi.inf.bsc.sa4.lab02spring.model.Decoration;
import ch.usi.inf.bsc.sa4.lab02spring.model.ExitDoor;
import ch.usi.inf.bsc.sa4.lab02spring.model.GameObject;
import ch.usi.inf.bsc.sa4.lab02spring.model.Position;
import ch.usi.inf.bsc.sa4.lab02spring.model.Slime;
import ch.usi.inf.bsc.sa4.lab02spring.model.Snail;
import ch.usi.inf.bsc.sa4.lab02spring.model.StartFlag;
import ch.usi.inf.bsc.sa4.lab02spring.service.GameObjectFactory.ObjectTypeEnum;
import ch.usi.inf.bsc.sa4.lab02spring.utils.UnknownObjectTypeException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.stream.Stream;

/// Black-box tests for [GameObjectFactory].
/// Verifies that the factory correctly dispatches tile-type strings
/// to the corresponding [GameObject] subclasses.
@SpringBootTest
@SuppressWarnings("PMD.TooManyStaticImports")
@DisplayName("The Game Object Factory")
class GameObjectFactoryTest {

    /// Tile gid used by all dispatch fixtures.
    private static final int GID = 42;

    /// Position used by all dispatch fixtures.
    private static final Position POS = new Position(1, 2);

    /// Tile-type string for the Box variant.
    private static final String TYPE_BOX = "Box";

    /// Tile-type string used to verify unknown-type rejection.
    private static final String TYPE_UNKNOWN = "Not_A_Real_Type";

    /// The factory under test.
    @Autowired
    private GameObjectFactory gameObjectFactory;

    /// Mocked tileset service used to resolve gids to tile-type strings.
    @MockitoBean
    private TileSetService tileSetService;

    /// Content used for box-type dispatch tests.
    private static final Content BOX_CONTENT = new Content.SomeContent(CoinType.GOLD_COIN);

    /// Content used for non-box dispatch tests.
    private static final Content NO_CONTENT = new Content.NoContent();

    /// Provides one row per ObjectTypeEnum constant: the json string,
    /// the expected concrete class, and the content to pass in.
    private static Stream<Arguments> dispatchCases() {
        return Stream.of(
                Arguments.of("Decoration", Decoration.class, NO_CONTENT),
                Arguments.of("Enemy_Slime_Normal", Slime.class, NO_CONTENT),
                Arguments.of("Enemy_Snail", Snail.class, NO_CONTENT),
                Arguments.of(TYPE_BOX, Box.class, BOX_CONTENT),
                Arguments.of("BoxDouble", Box.class, BOX_CONTENT),
                Arguments.of("Start_Flag", StartFlag.class, NO_CONTENT),
                Arguments.of("Door_Closed", ExitDoor.class, NO_CONTENT),
                Arguments.of("Item_Coin_Gold", Coin.class, NO_CONTENT),
                Arguments.of("Item_Coin_Silver", Coin.class, NO_CONTENT),
                Arguments.of("Item_Coin_Bronze", Coin.class, NO_CONTENT)
        );
    }

    /// Tests for the [ObjectTypeEnum] dispatch logic.
    @Nested
    @DisplayName("ObjectTypeEnum dispatch")
    class EnumDispatch {

        /// Each enum constant must build the expected [GameObject] subtype.
        @ParameterizedTest(name = "{0} -> {1}")
        @MethodSource("ch.usi.inf.bsc.sa4.lab02spring.service.GameObjectFactoryTest#dispatchCases")
        @DisplayName("dispatches each tile type to the right class")
        void dispatchesToCorrectClass(
                final String tileType,
                final Class<? extends GameObject> expected,
                final Content content) {
            final GameObject result = ObjectTypeEnum.fromValue(tileType)
                    .createGameObject(GID, POS, content);

            Assertions.assertInstanceOf(expected, result);
        }

        /// The dispatched [GameObject] must preserve the supplied gid.
        @ParameterizedTest(name = "{0} preserves gid")
        @MethodSource("ch.usi.inf.bsc.sa4.lab02spring.service.GameObjectFactoryTest#dispatchCases")
        @DisplayName("preserves gid for each tile type")
        void preservesGid(
                final String tileType,
                final Class<? extends GameObject> expected,
                final Content content) {
            final GameObject result = ObjectTypeEnum.fromValue(tileType)
                    .createGameObject(GID, POS, content);

            Assertions.assertEquals(GID, result.gid());
        }

        /// The dispatched [GameObject] must preserve the supplied position.
        @ParameterizedTest(name = "{0} preserves pos")
        @MethodSource("ch.usi.inf.bsc.sa4.lab02spring.service.GameObjectFactoryTest#dispatchCases")
        @DisplayName("preserves position for each tile type")
        void preservesPosition(
                final String tileType,
                final Class<? extends GameObject> expected,
                final Content content) {
            final GameObject result = ObjectTypeEnum.fromValue(tileType)
                    .createGameObject(GID, POS, content);

            Assertions.assertEquals(POS, result.pos());
        }

        /// fromValue must reject any tile-type string not registered in the enum.
        @Test
        @DisplayName("rejects unregistered tile-type strings")
        void rejectsUnknownType() {
            Assertions.assertThrows(UnknownObjectTypeException.class,
                    () -> ObjectTypeEnum.fromValue(TYPE_UNKNOWN));
        }
    }

    /// Tests for specific tile-type behavior.
    @Nested
    @DisplayName("specific tile types")
    class SpecificTypes {

        /// [Box] must carry the content passed by the factory caller.
        @Test
        @DisplayName("Box propagates the supplied content")
        void boxCarriesContent() {
            final Content content = new Content.SomeContent(CoinType.SILVER_COIN);

            final Box box = (Box) ObjectTypeEnum.fromValue(TYPE_BOX)
                    .createGameObject(GID, POS, content);

            Assertions.assertEquals(content, box.content());
        }

        /// The gold-coin constant must hardcode CoinType.GOLD_COIN.
        @Test
        @DisplayName("Gold coin encodes GOLD_COIN type")
        void goldCoinEncodesGold() {
            final Coin coin = (Coin) ObjectTypeEnum.fromValue("Item_Coin_Gold")
                    .createGameObject(GID, POS, NO_CONTENT);

            Assertions.assertEquals(CoinType.GOLD_COIN, coin.type());
        }

        /// The silver-coin constant must hardcode CoinType.SILVER_COIN.
        @Test
        @DisplayName("Silver coin encodes SILVER_COIN type")
        void silverCoinEncodesSilver() {
            final Coin coin = (Coin) ObjectTypeEnum.fromValue("Item_Coin_Silver")
                    .createGameObject(GID, POS, NO_CONTENT);

            Assertions.assertEquals(CoinType.SILVER_COIN, coin.type());
        }

        /// The bronze-coin constant must hardcode CoinType.BRONZE_COIN.
        @Test
        @DisplayName("Bronze coin encodes BRONZE_COIN type")
        void bronzeCoinEncodesBronze() {
            final Coin coin = (Coin) ObjectTypeEnum.fromValue("Item_Coin_Bronze")
                    .createGameObject(GID, POS, NO_CONTENT);

            Assertions.assertEquals(CoinType.BRONZE_COIN, coin.type());
        }
    }

    /// Tests for the @JsonValue accessor.
    @Nested
    @DisplayName("@JsonValue serialization")
    class JsonValue {

        /// The accessor must echo the BOX constant's wire string.
        @Test
        @DisplayName("returns the JSON tile-type string for BOX")
        void boxValue() {
            Assertions.assertEquals(TYPE_BOX, ObjectTypeEnum.BOX.value());
        }

        /// The accessor must echo the gold-coin wire string.
        @Test
        @DisplayName("returns the JSON tile-type string for ITEM_COIN_GOLD")
        void goldCoinValue() {
            Assertions.assertEquals("Item_Coin_Gold",
                    ObjectTypeEnum.ITEM_COIN_GOLD.value());
        }
    }

    /// Tests for the factory's public methods.
    @Nested
    @DisplayName("createGameObject")
    class CreateGameObject {

        /// The 3-arg entry point must dispatch to the type returned by [TileSetService].
        @Test
        @DisplayName("dispatches by resolved tile type with 3 arguments")
        void threeArgDispatchesType() {
            Mockito.when(tileSetService.getObjectTileType(GID))
                    .thenReturn(TYPE_BOX);

            final GameObject result = gameObjectFactory.createGameObject(GID, POS,
                    new Content.SomeContent(CoinType.BRONZE_COIN));

            Assertions.assertInstanceOf(Box.class, result);
        }

        /// The 3-arg entry point must propagate the caller's content into the box.
        @Test
        @DisplayName("propagates content into the box with 3 arguments")
        void threeArgPropagatesContent() {
            Mockito.when(tileSetService.getObjectTileType(GID))
                    .thenReturn(TYPE_BOX);
            final Content content = new Content.SomeContent(CoinType.BRONZE_COIN);

            final Box box = (Box) gameObjectFactory.createGameObject(GID, POS, content);

            Assertions.assertEquals(content, box.content());
        }

        /// The 2-arg overload must default content to NoContent.
        @Test
        @DisplayName("defaults content to NoContent with 2 arguments")
        void twoArgDefaultsToNoContent() {
            Mockito.when(tileSetService.getObjectTileType(GID))
                    .thenReturn(TYPE_BOX);

            final Box box = (Box) gameObjectFactory.createGameObject(GID, POS);

            Assertions.assertEquals(new Content.NoContent(), box.content());
        }

        /// An unknown tile-type string from the tileset service must
        /// surface as [UnknownObjectTypeException].
        @Test
        @DisplayName("propagates unknown tile types as exception")
        void propagatesUnknownType() {
            Mockito.when(tileSetService.getObjectTileType(GID))
                    .thenReturn(TYPE_UNKNOWN);

            Assertions.assertThrows(UnknownObjectTypeException.class,
                    () -> gameObjectFactory.createGameObject(GID, POS));
        }
    }
}
