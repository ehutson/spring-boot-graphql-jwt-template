package dev.ehutson.template.graphql.datafetcher;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.InputArgument;
import dev.ehutson.template.codegen.types.AuthPayload;
import dev.ehutson.template.codegen.types.LoginInput;
import dev.ehutson.template.codegen.types.RegisterInput;
import dev.ehutson.template.domain.UserModel;
import dev.ehutson.template.exception.CustomException;
import dev.ehutson.template.mapper.UserMapper;
import dev.ehutson.template.repository.UserRepository;
import dev.ehutson.template.security.service.AuthenticationService;
import dev.ehutson.template.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;

import static dev.ehutson.template.util.ServletRequestUtil.getRequest;
import static dev.ehutson.template.util.ServletRequestUtil.getResponse;

@Slf4j
@DgsComponent
@RequiredArgsConstructor
public class AuthDataFetcher {
    private final UserMapper userMapper;

    private final UserRepository userRepository;
    private final AuthenticationService authenticationService;
    private final UserService userService;


    @DgsMutation
    public AuthPayload register(@InputArgument RegisterInput input) {
        log.info("Registering user {}", input.getUsername());

        try {
            UserModel savedUser = userService.registerUser(input, getRequest(), getResponse());

            return AuthPayload.newBuilder()
                    .user(userMapper.toUser(savedUser))
                    .success(true)
                    .message("User registered successfully")
                    .build();
        } catch (CustomException e) {
            return AuthPayload.newBuilder()
                    .success(false)
                    .message(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("Registration error", e);
            return AuthPayload.newBuilder()
                    .success(false)
                    .message("Registration failed: " + e.getMessage())
                    .build();
        }
    }

    @DgsMutation
    public AuthPayload login(@InputArgument LoginInput input) {
        log.info("Logging user {}", input.getUsername());

        try {
            authenticationService.authenticate(input.getUsername(), input.getPassword(), getRequest(), getResponse());

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
            authenticationService.refreshToken(getRequest(), getResponse());

            String username = getRequest().getUserPrincipal().getName();
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
            authenticationService.logout(getRequest(), getResponse());
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
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            UserModel userModel = userRepository.findOneByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            authenticationService.revokeAllSessions(userModel.getId(), getResponse());
            return true;
        } catch (Exception e) {
            log.error("Revoke all sessions error", e);
            return false;
        }
    }

}
