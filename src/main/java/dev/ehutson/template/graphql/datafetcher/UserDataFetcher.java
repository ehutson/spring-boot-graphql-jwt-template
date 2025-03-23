package dev.ehutson.template.graphql.datafetcher;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import dev.ehutson.template.codegen.types.*;
import dev.ehutson.template.domain.RoleModel;
import dev.ehutson.template.domain.UserModel;
import dev.ehutson.template.exception.UserNotFoundException;
import dev.ehutson.template.mapper.RoleMapper;
import dev.ehutson.template.mapper.UserMapper;
import dev.ehutson.template.repository.RoleRepository;
import dev.ehutson.template.repository.UserRepository;
import dev.ehutson.template.security.service.AuthorizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@DgsComponent
@RequiredArgsConstructor
public class UserDataFetcher {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final AuthorizationService authorizationService;

    @DgsQuery(field = "me")
    public User getCurrentUser() {
        return authorizationService.getCurrentUser().map(userMapper::toUser)
                .orElseThrow(() -> new AccessDeniedException("Not authenticated"));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or @authorizationService.isResourceOwner(#id)")
    @DgsQuery(field = "user")
    public User getUser(@InputArgument String id) {
        return userRepository.findById(id).map(userMapper::toUser)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DgsQuery(field = "users")
    public List<User> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toUser)
                .toList();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DgsQuery(field = "roles")
    public List<Role> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(roleMapper::toRole)
                .toList();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DgsMutation
    public User createUser(@InputArgument CreateUserInput input) {
        if (userRepository.existsByUsername(input.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmail(input.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        UserModel user = userMapper.toUserModel(input);
        user.setPassword(passwordEncoder.encode(input.getPassword()));

        List<RoleModel> roles = new ArrayList<>();
        if (input.getRoles() != null && !input.getRoles().isEmpty()) {
            for (String roleName : input.getRoles()) {
                RoleModel roleModel = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new RuntimeException("Role not found " + roleName));
                roles.add(roleModel);
            }
        } else {
            RoleModel userRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new RuntimeException("Default role not found"));
            roles.add(userRole);
        }
        user.setRoles(roles);

        return userMapper.toUser(userRepository.save(user));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or @authorizationService.isResourceOwner(#id)")
    @DgsMutation
    public User updateUser(@InputArgument String id, @InputArgument UpdateUserInput input) {
        UserModel userModel = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (input.getUsername() != null && !input.getUsername().equals(userModel.getUsername()) && userRepository.existsByUsername(input.getUsername())) {
            throw new RuntimeException("Username is already taken");
        }

        if (input.getEmail() != null && !input.getEmail().equals(userModel.getEmail())
                && userRepository.existsByEmail(input.getEmail())) {
            throw new RuntimeException("Email is already taken");
        }

        if (input.getUsername() != null) {
            userModel.setUsername(input.getUsername());
        }

        if (input.getEmail() != null) {
            userModel.setEmail(input.getEmail());
        }
        if (input.getPassword() != null) {
            userModel.setPassword(passwordEncoder.encode(input.getPassword()));
        }

        if (input.getEnabled() != null) {
            userModel.setActivated(input.getEnabled());
        }

        if (input.getFirstName() != null) {
            userModel.setFirstName(input.getFirstName());
        }

        if (input.getLastName() != null) {
            userModel.setLastName(input.getLastName());
        }

        if (input.getLangKey() != null) {
            userModel.setLangKey(input.getLangKey());
        }

        return userMapper.toUser(userRepository.save(userModel));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DgsMutation
    public boolean deleteUser(@InputArgument String id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found");
        }
        userRepository.deleteById(id);
        return true;
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DgsMutation
    public Role createRole(@InputArgument("input") CreateRoleInput input) {
        if (roleRepository.findByName(input.getName()).isPresent()) {
            throw new RuntimeException("Role already exists");
        }
        return roleMapper.toRole(roleRepository.save(roleMapper.toRoleModel(input)));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DgsMutation
    public Role updateRole(@InputArgument String id, @InputArgument("input") UpdateRoleInput input) {
        RoleModel roleModel = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));

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
                .orElseThrow(() -> new RuntimeException("Role not found"));

        if (userRepository.existsByRolesContaining(roleModel)) {
            throw new RuntimeException("Role is assigned to one or more users and cannot be deleted");
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
