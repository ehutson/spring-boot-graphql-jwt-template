package dev.ehutson.template.util;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class MongoOffsetDateTimeConverterTest {

    private final MongoOffsetDateTimeConverter converter = new MongoOffsetDateTimeConverter();

    @Test
    void testConvert_withValidOffsetDateTime_shouldReturnInstant() {
        // Arrange
        OffsetDateTime now = OffsetDateTime.now();
        
        // Act
        Instant result = converter.convert(now);
        
        // Assert
        assertNotNull(result);
        assertEquals(now.toInstant().toEpochMilli(), result.toEpochMilli());
    }

    @Test
    void testConvert_withNullOffsetDateTime_shouldReturnNull() {
        // Act
        Instant result = converter.convert(null);
        
        // Assert
        assertNull(result);
    }
    
    @Test
    void testConvert_withOffsetDateTimeZero_shouldReturnEpochStart() {
        // Arrange
        OffsetDateTime epochStart = OffsetDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        
        // Act
        Instant result = converter.convert(epochStart);
        
        // Assert
        assertNotNull(result);
        assertEquals(Instant.EPOCH, result);
    }
    
    @Test
    void testConvert_withDifferentTimeZones_shouldReturnSameInstant() {
        // Arrange
        OffsetDateTime utcTime = OffsetDateTime.of(2023, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime estTime = utcTime.withOffsetSameInstant(ZoneOffset.ofHours(-5));
        
        // Act
        Instant result1 = converter.convert(utcTime);
        Instant result2 = converter.convert(estTime);
        
        // Assert
        assertEquals(result1, result2);
    }
    
    @Test
    void testInstance_shouldReturnSameConversionAsNewInstance() {
        // Arrange
        OffsetDateTime now = OffsetDateTime.now();
        
        // Act
        Instant result1 = converter.convert(now);
        Instant result2 = MongoOffsetDateTimeConverter.INSTANCE.convert(now);
        
        // Assert
        assertEquals(result1, result2);
    }
}