package dev.ehutson.template.graphql.datafetcher;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import dev.ehutson.template.codegen.types.*;
import dev.ehutson.template.domain.RoleModel;
import dev.ehutson.template.domain.UserModel;
import dev.ehutson.template.exception.ApplicationException;
import dev.ehutson.template.exception.ErrorCode;
import dev.ehutson.template.mapper.UserMapper;
import dev.ehutson.template.repository.RoleRepository;
import dev.ehutson.template.repository.UserRepository;
import dev.ehutson.template.security.service.AuthorizationService;
import dev.ehutson.template.service.pagination.PaginationService;
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

    private static final String USER_NOT_FOUND = "User not found";
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final AuthorizationService authorizationService;
    private final PaginationService paginationService;

    @DgsQuery(field = "me")
    public User getCurrentUser() {
        return authorizationService.getCurrentUser().map(userMapper::toUser)
                .orElseThrow(() -> ApplicationException.of(ErrorCode.INSUFFICIENT_PRIVILEGES));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or @authorizationService.isResourceOwner(#id)")
    @DgsQuery(field = "user")
    public User getUser(@InputArgument String id) {
        return userRepository.findById(id).map(userMapper::toUser)
                .orElseThrow(() -> ApplicationException.of(ErrorCode.RESOURCE_NOT_FOUND, USER_NOT_FOUND, "User", id));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DgsQuery(field = "users")
    public UserConnection getAllUsers(@InputArgument PaginationInput input) {
        Page<UserModel> userPage = paginationService.getPage(
                input,
                userRepository::findAll
        );

        List<UserEdge> edges = userPage.getContent().stream()
                .map(user -> getUserEdge(user, userPage))
                .toList();

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

    private UserEdge getUserEdge(UserModel user, Page<UserModel> userPage) {
        int index = userPage.getContent().indexOf(user);
        long offset = (long) userPage.getNumber() * userPage.getSize() + index;
        String cursor = paginationService.encodeCursor(offset);
        return UserEdge.newBuilder()
                .cursor(cursor)
                .node(userMapper.toUser(user))
                .build();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DgsMutation
    public User createUser(@InputArgument CreateUserInput input) {
        if (userRepository.existsByUsername(input.getUsername())) {
            throw ApplicationException.of(ErrorCode.RESOURCE_ALREADY_EXISTS,
                    "Username already exists", "User", "Username", input.getUsername());
        }

        if (userRepository.existsByEmail(input.getEmail())) {
            throw ApplicationException.of(ErrorCode.RESOURCE_ALREADY_EXISTS,
                    "Email already exists", "User", "Email Address", input.getEmail());
        }

        UserModel user = userMapper.toUserModel(input);
        user.setPassword(passwordEncoder.encode(input.getPassword()));

        List<RoleModel> roles = new ArrayList<>();
        if (input.getRoles() != null && !input.getRoles().isEmpty()) {
            for (String roleName : input.getRoles()) {
                RoleModel roleModel = roleRepository.findByName(roleName)
                        .orElseThrow(() -> ApplicationException.of(ErrorCode.RESOURCE_NOT_FOUND, "Role", roleName));
                roles.add(roleModel);
            }
        } else {
            RoleModel userRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> ApplicationException.of(ErrorCode.RESOURCE_NOT_FOUND,
                            "Default Role not found", "Default Role", "ROLE_USER"));
            roles.add(userRole);
        }
        user.setRoles(roles);

        return userMapper.toUser(userRepository.save(user));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or @authorizationService.isResourceOwner(#id)")
    @DgsMutation
    public User updateUser(@InputArgument String id, @InputArgument UpdateUserInput input) {
        UserModel userModel = userRepository.findById(id)
                .orElseThrow(() -> ApplicationException.of(ErrorCode.RESOURCE_NOT_FOUND, USER_NOT_FOUND, "User", id));

        if (input.getUsername() != null
                && !input.getUsername().equals(userModel.getUsername())
                && userRepository.existsByUsername(input.getUsername())) {
            throw ApplicationException.of(ErrorCode.RESOURCE_ALREADY_EXISTS,
                    "Username already exists", "User", "Username", input.getUsername());
        }

        if (input.getEmail() != null && !input.getEmail().equals(userModel.getEmail())
                && userRepository.existsByEmail(input.getEmail())) {
            throw ApplicationException.of(ErrorCode.RESOURCE_ALREADY_EXISTS,
                    "Email already exists", "User", "Email Address", input.getEmail());
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

        if (input.getFirstName() != null) {
            userModel.setFirstName(input.getFirstName());
        }

        if (input.getLastName() != null) {
            userModel.setLastName(input.getLastName());
        }

        if (input.getLangKey() != null) {
            userModel.setLangKey(input.getLangKey());
        }

        if (input.getTimezone() != null) {
            userModel.setTimezone(input.getTimezone());
        }

        return userMapper.toUser(userRepository.save(userModel));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DgsMutation
    public boolean deleteUser(@InputArgument String id) {
        if (!userRepository.existsById(id)) {
            throw ApplicationException.of(ErrorCode.RESOURCE_NOT_FOUND, USER_NOT_FOUND, "User", id);
        }
        userRepository.deleteById(id);
        return true;
    }
}
