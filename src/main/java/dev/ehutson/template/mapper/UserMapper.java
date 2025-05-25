package dev.ehutson.template.mapper;

import dev.ehutson.template.codegen.types.CreateUserInput;
import dev.ehutson.template.codegen.types.Role;
import dev.ehutson.template.codegen.types.User;
import dev.ehutson.template.domain.RoleModel;
import dev.ehutson.template.domain.UserModel;
import dev.ehutson.template.mapper.config.DateTimeMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", config = DateTimeMapperConfig.class)
public interface UserMapper {

    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "instantToOffsetDateTime")
    @Mapping(target = "lastModifiedAt", source = "lastModifiedAt", qualifiedByName = "instantToOffsetDateTime")
    @Mapping(target = "resetDate", source = "resetDate", qualifiedByName = "instantToOffsetDateTime")
    @Mapping(target = "activationDate", source = "activationDate", qualifiedByName = "instantToOffsetDateTime")
        //@Mapping(target = "roles.createdAt", source = "roles.createdAt", qualifiedByName = "instantToOffsetDateTime")
    User toUser(UserModel userModel);

    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "instantToOffsetDateTime")
    @Mapping(target = "lastModifiedAt", source = "lastModifiedAt", qualifiedByName = "instantToOffsetDateTime")
    @Mapping(target = "resetDate", source = "resetDate", qualifiedByName = "instantToOffsetDateTime")
    @Mapping(target = "roles.createdAt", source = "roles.createdAt", qualifiedByName = "instantToOffsetDateTime")
    List<User> toUsers(List<UserModel> userModelList);

    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "instantToOffsetDateTime")
    @Mapping(target = "lastModifiedAt", source = "lastModifiedAt", qualifiedByName = "instantToOffsetDateTime")
    Role toRole(RoleModel roleModel);

    List<Role> toRoles(List<RoleModel> roleModels);

    @Mapping(target = "resetKey", ignore = true)
    @Mapping(target = "resetDate", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "activationKey", ignore = true)
    @Mapping(target = "activationDate", ignore = true)
    @Mapping(target = "activated", ignore = true)
    @Mapping(target = "roles", ignore = true)
    UserModel toUserModel(CreateUserInput user);

}