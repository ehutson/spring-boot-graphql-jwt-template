package dev.ehutson.template.mapper;

import dev.ehutson.template.codegen.types.CreateRoleInput;
import dev.ehutson.template.codegen.types.Role;
import dev.ehutson.template.domain.RoleModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    // Convert List<RoleModel> to List<Role>
    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "instantToOffsetDateTime")
    @Mapping(target = "lastModifiedAt", source = "lastModifiedAt", qualifiedByName = "instantToOffsetDateTime")
    List<Role> mapRoles(List<RoleModel> roles);

    // Map individual RoleModel to Role
    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "instantToOffsetDateTime")
    @Mapping(target = "lastModifiedAt", source = "lastModifiedAt", qualifiedByName = "instantToOffsetDateTime")
    Role toRole(RoleModel roleModel);

    @Mapping(target = "predefined", ignore = true)
    @Mapping(target = "id", ignore = true)
    RoleModel toRoleModel(CreateRoleInput role);

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
