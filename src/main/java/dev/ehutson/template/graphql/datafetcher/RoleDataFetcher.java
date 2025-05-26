package dev.ehutson.template.graphql.datafetcher;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import dev.ehutson.template.codegen.types.CreateRoleInput;
import dev.ehutson.template.codegen.types.Role;
import dev.ehutson.template.codegen.types.UpdateRoleInput;
import dev.ehutson.template.codegen.types.User;
import dev.ehutson.template.domain.RoleModel;
import dev.ehutson.template.exception.ApplicationException;
import dev.ehutson.template.exception.ErrorCode;
import dev.ehutson.template.mapper.RoleMapper;
import dev.ehutson.template.mapper.UserMapper;
import dev.ehutson.template.repository.RoleRepository;
import dev.ehutson.template.repository.UserRepository;
import dev.ehutson.template.security.service.AuthorizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@Slf4j
@DgsComponent
@RequiredArgsConstructor
public class RoleDataFetcher {

    private static final String ROLE_NOT_FOUND = "Role not found";
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final AuthorizationService authorizationService;


    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DgsQuery(field = "roles")
    public List<Role> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(roleMapper::toRole)
                .toList();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DgsMutation
    public Role createRole(@InputArgument("input") CreateRoleInput input) {
        if (roleRepository.findByName(input.getName()).isPresent()) {
            throw ApplicationException.of(ErrorCode.RESOURCE_ALREADY_EXISTS, "Role already exists", "User", "Role", input.getName());
        }
        return roleMapper.toRole(roleRepository.save(roleMapper.toRoleModel(input)));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DgsMutation
    public Role updateRole(@InputArgument String id, @InputArgument("input") UpdateRoleInput input) {
        RoleModel roleModel = roleRepository.findById(id)
                .orElseThrow(() -> ApplicationException.of(ErrorCode.RESOURCE_NOT_FOUND, "Role", id));

        if (input.getName() != null) {
            roleModel.setName(input.getName());
        }

        if (input.getDescription() != null) {
            roleModel.setDescription(input.getDescription());
        }

        return roleMapper.toRole(roleRepository.save(roleModel));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DgsMutation
    public boolean deleteRole(@InputArgument String id) {
        RoleModel roleModel = roleRepository.findById(id)
                .orElseThrow(() -> ApplicationException.of(ErrorCode.RESOURCE_NOT_FOUND, "Role", id));

        if (userRepository.existsByRolesContaining(roleModel)) {
            throw ApplicationException.of(ErrorCode.VALIDATION_FAILED, "Role assigned",
                    "Role is assigned to one or more users and cannot be deleted");
        }

        roleRepository.deleteById(id);
        return true;
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DgsMutation
    public User assignRoleToUser(@InputArgument String userId, @InputArgument String roleId) {
        return userMapper.toUser(authorizationService.assignRoleToUser(userId, roleId));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DgsMutation
    public User removeRoleFromUser(@InputArgument String userId, @InputArgument String roleId) {
        return userMapper.toUser(authorizationService.removeRoleFromUser(userId, roleId));
    }

}
