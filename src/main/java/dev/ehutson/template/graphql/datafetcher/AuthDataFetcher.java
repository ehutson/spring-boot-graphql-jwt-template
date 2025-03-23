package dev.ehutson.template.graphql.datafetcher;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.InputArgument;
import dev.ehutson.template.codegen.types.AuthPayload;
import dev.ehutson.template.codegen.types.LoginInput;
import dev.ehutson.template.codegen.types.RegisterInput;
import dev.ehutson.template.domain.RoleModel;
import dev.ehutson.template.domain.UserModel;
import dev.ehutson.template.mapper.UserMapper;
import dev.ehutson.template.repository.RoleRepository;
import dev.ehutson.template.repository.UserRepository;
import dev.ehutson.template.security.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@DgsComponent
@RequiredArgsConstructor
public class AuthDataFetcher {
    private final UserMapper userMapper;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationService authenticationService;


    @DgsMutation
    public AuthPayload register(@InputArgument RegisterInput input) {
        log.info("Registering user {}", input.getUsername());

        if (userRepository.existsByUsername(input.getUsername())) {
            return AuthPayload.newBuilder()
                    .success(false)
                    .message("Username already exists")
                    .build();
        }

        if (userRepository.existsByEmail(input.getEmail())) {
            return AuthPayload.newBuilder()
                    .success(false)
                    .message("Email is already in use")
                    .build();
        }

        UserModel user = new UserModel();
        user.setUsername(input.getUsername());
        user.setEmail(input.getEmail());
        user.setPassword(passwordEncoder.encode(input.getPassword()));
        user.setFirstName(input.getFirstName());
        user.setLastName(input.getLastName());
        user.setLangKey(input.getLangKey());

        List<RoleModel> roles = new ArrayList<>();
        RoleModel userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Default role not found"));
        roles.add(userRole);
        user.setRoles(roles);

        UserModel savedUser = userRepository.save(user);

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        HttpServletResponse response = attributes.getResponse();
        authenticationService.authenticate(input.getUsername(), input.getPassword(), request, response);

        return AuthPayload.newBuilder()
                .user(userMapper.toUser(savedUser))
                .success(true)
                .message("User registered successfully")
                .build();
    }

    @DgsMutation
    public AuthPayload login(@InputArgument LoginInput input, DgsDataFetchingEnvironment env) {
        log.info("Logging user {}", input.getUsername());

        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attributes.getRequest();
            HttpServletResponse response = attributes.getResponse();

            authenticationService.authenticate(input.getUsername(), input.getPassword(), request, response);

            UserModel user = userRepository.findOneByUsername(input.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            return AuthPayload.newBuilder()
                    .user(userMapper.toUser(user))
                    .success(true)
                    .message("Authentication successful")
                    .build();
        } catch (Exception e) {
            log.error("Authentication error", e);
            return AuthPayload.newBuilder()
                    .success(false)
                    .message("Invalid username or password")
                    .build();
        }
    }

    @DgsMutation
    public AuthPayload refreshToken() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attributes.getRequest();
            HttpServletResponse response = attributes.getResponse();

            authenticationService.refreshToken(request, response);

            String username = request.getUserPrincipal().getName();
            UserModel user = userRepository.findOneByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            return AuthPayload.newBuilder()
                    .user(userMapper.toUser(user))
                    .success(true)
                    .message("Token refreshed successfully")
                    .build();
        } catch (Exception e) {
            log.error("Refresh token error", e);
            return AuthPayload.newBuilder()
                    .success(false)
                    .message("Failed to refresh token:  " + e.getMessage())
                    .build();
        }
    }

    @DgsMutation
    public boolean logout() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attributes.getRequest();
            HttpServletResponse response = attributes.getResponse();

            authenticationService.logout(request, response);
            return true;
        } catch (Exception e) {
            log.error("Logout error", e);
            return false;
        }
    }

    @PreAuthorize("isAuthenticated()")
    @DgsMutation
    public boolean revokeAllSessions() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attributes.getRequest();
            HttpServletResponse response = attributes.getResponse();

            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            UserModel userModel = userRepository.findOneByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            authenticationService.revokeAllSessions(userModel.getId(), response);
            return true;
        } catch (Exception e) {
            log.error("Revoke all sessions error", e);
            return false;
        }
    }

}
