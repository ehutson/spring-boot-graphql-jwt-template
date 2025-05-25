package dev.ehutson.template.mapper;

import dev.ehutson.template.codegen.types.CreateRoleInput;
import dev.ehutson.template.codegen.types.Role;
import dev.ehutson.template.domain.RoleModel;
import dev.ehutson.template.mapper.config.DateTimeMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", config = DateTimeMapperConfig.class)
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
}
