package dev.ehutson.template.mapper;

import dev.ehutson.template.codegen.types.CreateUserInput;
import dev.ehutson.template.codegen.types.Role;
import dev.ehutson.template.codegen.types.User;
import dev.ehutson.template.domain.RoleModel;
import dev.ehutson.template.domain.UserModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Mapper(componentModel = "spring")
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