package dev.ehutson.template.graphql.datafetcher;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import dev.ehutson.template.codegen.types.*;
import dev.ehutson.template.domain.RoleModel;
import dev.ehutson.template.domain.UserModel;
import dev.ehutson.template.exception.InsufficientPrivilegesException;
import dev.ehutson.template.exception.ResourceAlreadyExistsException;
import dev.ehutson.template.exception.ResourceNotFoundException;
import dev.ehutson.template.exception.ValidationFailedException;
import dev.ehutson.template.mapper.RoleMapper;
import dev.ehutson.template.mapper.UserMapper;
import dev.ehutson.template.repository.RoleRepository;
import dev.ehutson.template.repository.UserRepository;
import dev.ehutson.template.security.service.AuthorizationService;
import dev.ehutson.template.service.PaginationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
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
    private final PaginationService paginationService;

    private static final String USER_NOT_FOUND = "User not found";
    private static final String ROLE_NOT_FOUND = "Role not found";

    @DgsQuery(field = "me")
    public User getCurrentUser() {
        return authorizationService.getCurrentUser().map(userMapper::toUser)
                .orElseThrow(InsufficientPrivilegesException::new);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or @authorizationService.isResourceOwner(#id)")
    @DgsQuery(field = "user")
    public User getUser(@InputArgument String id) {
        return userRepository.findById(id).map(userMapper::toUser)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND, "User", id));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DgsQuery(field = "users")
    public UserConnection getAllUsers(@InputArgument Integer first, @InputArgument String after, @InputArgument Integer last, @InputArgument String before) {
        Page<UserModel> userPage = paginationService.getPage(
                after, first, last,
                userRepository::findAll
        );

        List<UserEdge> edges = userPage.getContent().stream()
                .map(user -> {
                    int index = userPage.getContent().indexOf(user);
                    long offset = (long) userPage.getNumber() * userPage.getSize() + index;
                    String cursor = paginationService.encodeCursor(offset);
                    return UserEdge.newBuilder()
                            .cursor(cursor)
                            .node(userMapper.toUser(user))
                            .build();
                }).toList();

        PageInfo pageInfo = PageInfo.newBuilder()
                .hasNextPage(userPage.hasNext())
                .hasPreviousPage(userPage.hasPrevious())
                .startCursor(edges.isEmpty() ? null : edges.getFirst().getCursor())
                .endCursor(edges.isEmpty() ? null : edges.getLast().getCursor())
                .build();

        return UserConnection.newBuilder()
                .edges(edges)
                .pageInfo(pageInfo)
                .totalCount((int) userPage.getTotalElements())
                .build();
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
            throw new ResourceAlreadyExistsException("Username already exists", "User", "Username", input.getUsername());
        }

        if (userRepository.existsByEmail(input.getEmail())) {
            throw new ResourceAlreadyExistsException("Email already exists", "User", "Email Address", input.getEmail());
        }

        UserModel user = userMapper.toUserModel(input);
        user.setPassword(passwordEncoder.encode(input.getPassword()));

        List<RoleModel> roles = new ArrayList<>();
        if (input.getRoles() != null && !input.getRoles().isEmpty()) {
            for (String roleName : input.getRoles()) {
                RoleModel roleModel = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new ResourceNotFoundException(ROLE_NOT_FOUND, "Role", roleName));
                roles.add(roleModel);
            }
        } else {
            RoleModel userRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new ResourceNotFoundException("Default Role not found", "Default Role", "ROLE_USER"));
            roles.add(userRole);
        }
        user.setRoles(roles);

        return userMapper.toUser(userRepository.save(user));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or @authorizationService.isResourceOwner(#id)")
    @DgsMutation
    public User updateUser(@InputArgument String id, @InputArgument UpdateUserInput input) {
        UserModel userModel = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND, "User", id));

        if (input.getUsername() != null && !input.getUsername().equals(userModel.getUsername()) && userRepository.existsByUsername(input.getUsername())) {
            throw new ResourceAlreadyExistsException("Username already exists", "User", "Username", input.getUsername());
        }

        if (input.getEmail() != null && !input.getEmail().equals(userModel.getEmail())
                && userRepository.existsByEmail(input.getEmail())) {
            throw new ResourceAlreadyExistsException("Email already exists", "User", "Email Address", input.getEmail());
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
            throw new ResourceNotFoundException(USER_NOT_FOUND, "User", id);
        }
        userRepository.deleteById(id);
        return true;
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DgsMutation
    public Role createRole(@InputArgument("input") CreateRoleInput input) {
        if (roleRepository.findByName(input.getName()).isPresent()) {
            throw new ResourceAlreadyExistsException("Role already exists", "User", "Role", input.getName());
        }
        return roleMapper.toRole(roleRepository.save(roleMapper.toRoleModel(input)));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DgsMutation
    public Role updateRole(@InputArgument String id, @InputArgument("input") UpdateRoleInput input) {
        RoleModel roleModel = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ROLE_NOT_FOUND, "Role", id));

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
                .orElseThrow(() -> new ResourceNotFoundException(ROLE_NOT_FOUND, "Role", id));

        if (userRepository.existsByRolesContaining(roleModel)) {
            throw new ValidationFailedException("Role assigned",
                    "Role is assigned to one or more users and cannot be deleted"
            );
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
