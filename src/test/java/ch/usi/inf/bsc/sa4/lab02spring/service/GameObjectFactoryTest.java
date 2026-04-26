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
@SuppressWarnings({"NullAway", "PMD.AtLeastOneConstructor"})
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

    /// Each enum constant must build the expected GameObject subtype with the
    /// supplied gid and position preserved.
    @ParameterizedTest(name = "{0} -> {1}")
    @MethodSource("dispatchCases")
    @DisplayName("ObjectTypeEnum dispatches each tile type to its GameObject")
    void enumDispatchesToCorrectGameObject(
            final String tileType,
            final Class<? extends GameObject> expected,
            final Content content) {
        final GameObject result = ObjectTypeEnum.fromValue(tileType)
            .createGameObject(GID, POS, content);

        assertInstanceOf(expected, result);
        assertEquals(GID, result.gid());
        assertEquals(POS, result.pos());
    }

    /// Box must carry the content passed by the factory caller verbatim.
    @Test
    @DisplayName("Box dispatch propagates the supplied content")
    void boxDispatchCarriesContent() {
        final Content content = new Content.SomeContent(CoinType.SILVER_COIN);

        final GameObject result = ObjectTypeEnum.fromValue(TYPE_BOX)
            .createGameObject(GID, POS, content);

        final Box box = assertInstanceOf(Box.class, result);
        assertEquals(content, box.content());
    }

    /// Each gold/silver/bronze constant must hardcode its own coin variant.
    @Test
    @DisplayName("Coin dispatch encodes the coin variant per enum constant")
    void coinDispatchEncodesCoinType() {
        final Content noContent = new Content.NoContent();

        final Coin gold = (Coin) ObjectTypeEnum.fromValue("Item_Coin_Gold")
            .createGameObject(GID, POS, noContent);
        final Coin silver = (Coin) ObjectTypeEnum.fromValue("Item_Coin_Silver")
            .createGameObject(GID, POS, noContent);
        final Coin bronze = (Coin) ObjectTypeEnum.fromValue("Item_Coin_Bronze")
            .createGameObject(GID, POS, noContent);

        assertEquals(CoinType.GOLD_COIN, gold.type());
        assertEquals(CoinType.SILVER_COIN, silver.type());
        assertEquals(CoinType.BRONZE_COIN, bronze.type());
    }

    /// fromValue must reject any tile-type string not registered in the enum
    /// so unknown editor payloads fail fast.
    @Test
    @DisplayName("fromValue rejects unregistered tile-type strings")
    void fromValueRejectsUnknownType() {
        assertThrows(UnknownObjectTypeException.class,
            () -> ObjectTypeEnum.fromValue(TYPE_UNKNOWN));
    }

    /// The @JsonValue accessor must echo the constant's wire string so Jackson
    /// serializes the enum as the editor's tile-type identifier.
    @Test
    @DisplayName("value() returns the JSON tile-type string")
    void valueReturnsJsonTileType() {
        assertEquals(TYPE_BOX, ObjectTypeEnum.BOX.value());
        assertEquals("Item_Coin_Gold", ObjectTypeEnum.ITEM_COIN_GOLD.value());
    }

    /// The 3-arg factory entry point must resolve the gid via TileSetService
    /// and then dispatch through the enum.
    @Test
    @DisplayName("createGameObject(3-arg) delegates to TileSetService and enum")
    void createGameObjectThreeArgDispatches() {
        when(tileSetService.getObjectTileType(GID)).thenReturn(TYPE_BOX);
        final Content content = new Content.SomeContent(CoinType.BRONZE_COIN);

        final GameObject result = factory.createGameObject(GID, POS, content);

        final Box box = assertInstanceOf(Box.class, result);
        assertEquals(GID, box.gid());
        assertEquals(POS, box.pos());
        assertEquals(content, box.content());
    }

    /// The 2-arg overload must default content to NoContent so callers that
    /// do not care about box content (e.g. non-Box tiles) stay terse.
    @Test
    @DisplayName("createGameObject(2-arg) defaults content to NoContent")
    void createGameObjectTwoArgDefaultsToNoContent() {
        when(tileSetService.getObjectTileType(GID)).thenReturn(TYPE_BOX);

        final GameObject result = factory.createGameObject(GID, POS);

        final Box box = assertInstanceOf(Box.class, result);
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
