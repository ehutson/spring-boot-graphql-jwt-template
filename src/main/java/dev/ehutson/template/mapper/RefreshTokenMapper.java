package dev.ehutson.template.mapper;

import dev.ehutson.template.codegen.types.Session;
import dev.ehutson.template.domain.RefreshTokenModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = "spring")
public interface RefreshTokenMapper {
    @Mapping(target = "expirationDate", source = "expiresAt", qualifiedByName = "instantToOffsetDateTime")
    @Mapping(target = "lastUsed", source = "createdAt", qualifiedByName = "instantToOffsetDateTime")
    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "instantToOffsetDateTime")
    Session toSession(RefreshTokenModel refreshToken);

    /**
     * Converts an Instant to OffsetDateTime using UTC offset
     */
    @Named("instantToOffsetDateTime")
    default OffsetDateTime instantToOffsetDateTime(Instant instant) {
        if (instant == null) {
            return null;
        }
        return OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    /**
     * Converts an OffsetDateTime to Instant
     */
    @Named("offsetDateTimeToInstant")
    default Instant offsetDateTimeToInstant(OffsetDateTime offsetDateTime) {
        if (offsetDateTime == null) {
            return null;
        }
        return offsetDateTime.toInstant();
    }
}