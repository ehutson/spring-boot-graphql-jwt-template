package dev.ehutson.template.util;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class InstantToOffsetDateTimeConverterTest {

    private final InstantToOffsetDateTimeConverter converter = new InstantToOffsetDateTimeConverter();

    @Test
    void testConvert_withValidInstant_shouldReturnOffsetDateTimeInUTC() {
        // Arrange
        Instant now = Instant.now();
        
        // Act
        OffsetDateTime result = converter.convert(now);
        
        // Assert
        assertNotNull(result);
        assertEquals(ZoneOffset.UTC, result.getOffset());
        assertEquals(now.toEpochMilli(), result.toInstant().toEpochMilli());
    }

    @Test
    void testConvert_withNullInstant_shouldReturnNull() {
        // Act
        OffsetDateTime result = converter.convert(null);
        
        // Assert
        assertNull(result);
    }
    
    @Test
    void testConvert_withInstantZero_shouldReturnEpochStartInUTC() {
        // Arrange
        Instant epochInstant = Instant.EPOCH;
        
        // Act
        OffsetDateTime result = converter.convert(epochInstant);
        
        // Assert
        assertNotNull(result);
        assertEquals(ZoneOffset.UTC, result.getOffset());
        assertEquals(1970, result.getYear());
        assertEquals(1, result.getMonthValue());
        assertEquals(1, result.getDayOfMonth());
        assertEquals(0, result.getHour());
        assertEquals(0, result.getMinute());
        assertEquals(0, result.getSecond());
    }
    
    @Test
    void testInstance_shouldReturnSameConversionAsNewInstance() {
        // Arrange
        Instant now = Instant.now();
        
        // Act
        OffsetDateTime result1 = converter.convert(now);
        OffsetDateTime result2 = InstantToOffsetDateTimeConverter.INSTANCE.convert(now);
        
        // Assert
        assertEquals(result1, result2);
    }
}