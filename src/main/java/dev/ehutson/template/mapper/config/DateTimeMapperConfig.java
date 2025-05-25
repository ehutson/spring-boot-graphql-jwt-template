package dev.ehutson.template.mapper.config;

import org.mapstruct.MapperConfig;
import org.mapstruct.Named;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Shared configuration for MapStruct mappers containing common date/time conversion methods.
 * This is here to eliminate duplication across all mapper classes.
 */
@MapperConfig(componentModel = "spring")
public interface DateTimeMapperConfig {

    /**
     * Converts an Instant to OffsetDateTime using UTC offset
     *
     * @param instant The Instant
     * @return The OffsetDateTime
     */
    @Named("instantToOffsetDateTime")
    default OffsetDateTime instantToOffsetDateTime(Instant instant) {
        if (instant == null) {
            return null;
        }
        return OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    /**
     * Converts an OffsetDateTime to an Instant
     *
     * @param offsetDateTime The OffsetDateTime
     * @return The Instant
     */
    @Named("offsetDateTimeToInstant")
    default Instant offsetDateTimeToInstant(OffsetDateTime offsetDateTime) {
        if (offsetDateTime == null) {
            return null;
        }
        return offsetDateTime.toInstant();
    }
}
