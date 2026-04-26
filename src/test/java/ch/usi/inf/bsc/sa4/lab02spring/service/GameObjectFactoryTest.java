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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/// Unit tests for the GameObject factory and its tile-type dispatch enum.
@DisplayName("GameObjectFactory")
@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"NullAway", "PMD.AtLeastOneConstructor","PMD.TooManyStaticImports"})
class GameObjectFactoryTest {

    /// Tile gid used by all dispatch fixtures; the value itself is irrelevant.
    private static final int GID = 42;
    /// Position used by all dispatch fixtures.
    private static final Position POS = new Position(1, 2);
    /// Tile-type string for the Box variant.
    private static final String TYPE_BOX = "Box";
    /// Tile-type string used to verify unknown-type rejection.
    private static final String TYPE_UNKNOWN = "Not_A_Real_Type";

    /// Mocked tileset service used to resolve gids to tile-type strings.
    @Mock private TileSetService tileSetService;

    /// Factory under test, with mocks injected.
    @InjectMocks private GameObjectFactory factory;

    /// Provides one row per ObjectTypeEnum constant: the json string,
    /// the expected concrete class, and the content to pass in.
    /// Box variants need SomeContent; everything else uses NoContent.
    private static Stream<Arguments> dispatchCases() {
        final Content noContent = new Content.NoContent();
        final Content boxContent = new Content.SomeContent(CoinType.GOLD_COIN);
        return Stream.of(
            Arguments.of("Decoration", Decoration.class, noContent),
            Arguments.of("Enemy_Slime_Normal", Slime.class, noContent),
            Arguments.of("Enemy_Snail", Snail.class, noContent),
            Arguments.of(TYPE_BOX, Box.class, boxContent),
            Arguments.of("BoxDouble", Box.class, boxContent),
            Arguments.of("Start_Flag", StartFlag.class, noContent),
            Arguments.of("Door_Closed", ExitDoor.class, noContent),
            Arguments.of("Item_Coin_Gold", Coin.class, noContent),
            Arguments.of("Item_Coin_Silver", Coin.class, noContent),
            Arguments.of("Item_Coin_Bronze", Coin.class, noContent)
        );
    }

    /// Each enum constant must build the expected GameObject subtype.
    @ParameterizedTest(name = "{0} -> {1}")
    @MethodSource("dispatchCases")
    @DisplayName("ObjectTypeEnum dispatches each tile type to the right class")
    void enumDispatchesToCorrectClass(
            final String tileType,
            final Class<? extends GameObject> expected,
            final Content content) {
        final GameObject result = ObjectTypeEnum.fromValue(tileType)
            .createGameObject(GID, POS, content);

        assertInstanceOf(expected, result);
    }

    /// The dispatched GameObject must preserve the supplied gid.
    @ParameterizedTest(name = "{0} preserves gid")
    @MethodSource("dispatchCases")
    @DisplayName("ObjectTypeEnum dispatch preserves gid")
    void enumDispatchPreservesGid(
            final String tileType,
            final Class<? extends GameObject> expected,
            final Content content) {
        final GameObject result = ObjectTypeEnum.fromValue(tileType)
            .createGameObject(GID, POS, content);

        assertEquals(GID, result.gid());
    }

    /// The dispatched GameObject must preserve the supplied position.
    @ParameterizedTest(name = "{0} preserves pos")
    @MethodSource("dispatchCases")
    @DisplayName("ObjectTypeEnum dispatch preserves position")
    void enumDispatchPreservesPos(
            final String tileType,
            final Class<? extends GameObject> expected,
            final Content content) {
        final GameObject result = ObjectTypeEnum.fromValue(tileType)
            .createGameObject(GID, POS, content);

        assertEquals(POS, result.pos());
    }

    /// Box must carry the content passed by the factory caller verbatim.
    @Test
    @DisplayName("Box dispatch propagates the supplied content")
    void boxDispatchCarriesContent() {
        final Content content = new Content.SomeContent(CoinType.SILVER_COIN);

        final Box box = (Box) ObjectTypeEnum.fromValue(TYPE_BOX)
            .createGameObject(GID, POS, content);

        assertEquals(content, box.content());
    }

    /// The gold-coin constant must hardcode CoinType.GOLD_COIN.
    @Test
    @DisplayName("Gold coin dispatch encodes GOLD_COIN")
    void coinDispatchEncodesGold() {
        final Coin coin = (Coin) ObjectTypeEnum.fromValue("Item_Coin_Gold")
            .createGameObject(GID, POS, new Content.NoContent());

        assertEquals(CoinType.GOLD_COIN, coin.type());
    }

    /// The silver-coin constant must hardcode CoinType.SILVER_COIN.
    @Test
    @DisplayName("Silver coin dispatch encodes SILVER_COIN")
    void coinDispatchEncodesSilver() {
        final Coin coin = (Coin) ObjectTypeEnum.fromValue("Item_Coin_Silver")
            .createGameObject(GID, POS, new Content.NoContent());

        assertEquals(CoinType.SILVER_COIN, coin.type());
    }

    /// The bronze-coin constant must hardcode CoinType.BRONZE_COIN.
    @Test
    @DisplayName("Bronze coin dispatch encodes BRONZE_COIN")
    void coinDispatchEncodesBronze() {
        final Coin coin = (Coin) ObjectTypeEnum.fromValue("Item_Coin_Bronze")
            .createGameObject(GID, POS, new Content.NoContent());

        assertEquals(CoinType.BRONZE_COIN, coin.type());
    }

    /// fromValue must reject any tile-type string not registered in the enum
    /// so unknown editor payloads fail fast.
    @Test
    @DisplayName("fromValue rejects unregistered tile-type strings")
    void fromValueRejectsUnknownType() {
        assertThrows(UnknownObjectTypeException.class,
            () -> ObjectTypeEnum.fromValue(TYPE_UNKNOWN));
    }

    /// The @JsonValue accessor must echo the BOX constant's wire string.
    @Test
    @DisplayName("value() returns the JSON tile-type string for BOX")
    void valueReturnsJsonTileTypeForBox() {
        assertEquals(TYPE_BOX, ObjectTypeEnum.BOX.value());
    }

    /// The @JsonValue accessor must echo the gold-coin wire string.
    @Test
    @DisplayName("value() returns the JSON tile-type string for ITEM_COIN_GOLD")
    void valueReturnsJsonTileTypeForGold() {
        assertEquals("Item_Coin_Gold", ObjectTypeEnum.ITEM_COIN_GOLD.value());
    }

    /// The 3-arg entry point must dispatch to the type returned by TileSetService.
    @Test
    @DisplayName("createGameObject(3-arg) dispatches by resolved tile type")
    void createGameObjectThreeArgDispatchesType() {
        when(tileSetService.getObjectTileType(GID)).thenReturn(TYPE_BOX);

        final GameObject result = factory.createGameObject(GID, POS,
            new Content.SomeContent(CoinType.BRONZE_COIN));

        assertInstanceOf(Box.class, result);
    }

    /// The 3-arg entry point must propagate the caller's content into the box.
    @Test
    @DisplayName("createGameObject(3-arg) propagates content into the box")
    void createGameObjectThreeArgPropagatesContent() {
        when(tileSetService.getObjectTileType(GID)).thenReturn(TYPE_BOX);
        final Content content = new Content.SomeContent(CoinType.BRONZE_COIN);

        final Box box = (Box) factory.createGameObject(GID, POS, content);

        assertEquals(content, box.content());
    }

    /// The 2-arg overload must default content to NoContent so callers that
    /// do not care about box content (e.g. non-Box tiles) stay terse.
    @Test
    @DisplayName("createGameObject(2-arg) defaults content to NoContent")
    void createGameObjectTwoArgDefaultsToNoContent() {
        when(tileSetService.getObjectTileType(GID)).thenReturn(TYPE_BOX);

        final Box box = (Box) factory.createGameObject(GID, POS);

        assertEquals(new Content.NoContent(), box.content());
    }

    /// An unknown tile-type string returned by the tileset service must
    /// surface as UnknownObjectTypeException to the caller.
    @Test
    @DisplayName("createGameObject propagates unknown tile types as exception")
    void createGameObjectPropagatesUnknownType() {
        when(tileSetService.getObjectTileType(GID)).thenReturn(TYPE_UNKNOWN);

        assertThrows(UnknownObjectTypeException.class,
            () -> factory.createGameObject(GID, POS));
    }
}
