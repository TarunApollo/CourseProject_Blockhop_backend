package ch.usi.inf.bsc.sa4.lab02spring.configuration;

import ch.usi.inf.bsc.sa4.lab02spring.model.Position;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/// Unit tests for [MongoConfiguration] and its internal converters.
@DisplayName("Mongo Configuration")
class MongoConfigurationTests {

    /// The configuration instance under test.
    private MongoConfiguration mongoConfiguration;

    /// Sets up the test instance.
    @BeforeEach
    void setup() {
        this.mongoConfiguration = new MongoConfiguration();
    }

    /// Tests for the mapping context bean creation.
    @Nested
    @DisplayName("when configuring mapping context")
    class MappingContext {

        /// Verifies that a valid mapping context is created.
        @Test
        @DisplayName("should create a non-null mapping context")
        void testMappingContext() {
            final MongoCustomConversions conversions = mongoConfiguration.customConversions();
            final MongoMappingContext context = mongoConfiguration.mongoMappingContext(conversions);
            Assertions.assertNotNull(context);
        }
    }

    /// Tests for [Position] converters.
    @Nested
    @DisplayName("when converting positions")
    class PositionConversion {
        
        /// Converter from [Position] to [String].
        private final MongoConfiguration.PositionToStringConverter toStr = new MongoConfiguration.PositionToStringConverter();
        
        /// Converter from [String] to [Position].
        private final MongoConfiguration.StringToPositionConverter fromStr = new MongoConfiguration.StringToPositionConverter();

        /// Verifies [Position] to [String] conversion.
        @Test
        @DisplayName("should convert Position to String")
        void testPosToStr() {
            final Position pos = new Position(10, 20);
            Assertions.assertEquals("10,20", toStr.convert(pos));
        }

        /// Verifies [String] to [Position] conversion.
        @Test
        @DisplayName("should convert String to Position")
        void testStrToPos() {
            final Position pos = fromStr.convert("10,20");
            Assertions.assertEquals(10, pos.getX());
            Assertions.assertEquals(20, pos.getY());
        }
    }

    /// Tests for date converters.
    @Nested
    @DisplayName("when converting dates")
    class DateConversion {
        
        /// Converter from [ZonedDateTime] to [Instant].
        private final MongoConfiguration.ZonedDateTimeToInstantConverter toInstant = new MongoConfiguration.ZonedDateTimeToInstantConverter();
        
        /// Converter from [Instant] to [ZonedDateTime].
        private final MongoConfiguration.InstantToZonedDateTimeConverter fromInstant = new MongoConfiguration.InstantToZonedDateTimeConverter();

        /// Verifies [ZonedDateTime] to [Instant] conversion.
        @Test
        @DisplayName("should convert ZonedDateTime to Instant")
        void testZdtToInstant() {
            final ZonedDateTime zdt = ZonedDateTime.now();
            Assertions.assertEquals(zdt.toInstant(), toInstant.convert(zdt));
        }

        /// Verifies [Instant] to [ZonedDateTime] conversion.
        @Test
        @DisplayName("should convert Instant to ZonedDateTime in UTC")
        void testInstantToZdt() {
            final Instant instant = Instant.now();
            final ZonedDateTime zdt = fromInstant.convert(instant);
            Assertions.assertEquals(ZoneOffset.UTC, zdt.getZone());
            Assertions.assertEquals(instant.getEpochSecond(), zdt.toInstant().getEpochSecond());
        }
    }
}
