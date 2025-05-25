package dev.ehutson.template.mapper;

import dev.ehutson.template.codegen.types.Session;
import dev.ehutson.template.domain.RefreshTokenModel;
import dev.ehutson.template.mapper.config.DateTimeMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", config = DateTimeMapperConfig.class)
public interface RefreshTokenMapper {
    @Mapping(target = "expirationDate", source = "expiresAt", qualifiedByName = "instantToOffsetDateTime")
    @Mapping(target = "lastUsed", source = "createdAt", qualifiedByName = "instantToOffsetDateTime")
    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "instantToOffsetDateTime")
    Session toSession(RefreshTokenModel refreshToken);
}