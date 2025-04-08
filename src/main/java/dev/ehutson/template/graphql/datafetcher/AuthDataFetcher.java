package dev.ehutson.template.graphql.datafetcher;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.InputArgument;
import dev.ehutson.template.codegen.types.*;
import dev.ehutson.template.domain.UserModel;
import dev.ehutson.template.exception.ApplicationException;
import dev.ehutson.template.exception.ErrorCode;
import dev.ehutson.template.exception.ResourceNotFoundException;
import dev.ehutson.template.mapper.UserMapper;
import dev.ehutson.template.repository.UserRepository;
import dev.ehutson.template.security.service.AuthenticationService;
import dev.ehutson.template.service.MessageService;
import dev.ehutson.template.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;

import static dev.ehutson.template.util.ServletRequestUtil.getRequest;
import static dev.ehutson.template.util.ServletRequestUtil.getResponse;

@Slf4j
@DgsComponent
@RequiredArgsConstructor
public class AuthDataFetcher {
    private static final String USER_NOT_FOUND = "User not found";
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final AuthenticationService authenticationService;
    private final UserService userService;
    private final MessageService messageService;

    @DgsMutation
    public UserRegistrationResponse register(@InputArgument RegisterInput input) {
        log.info("Registering user {}", input.getUsername());

        try {
            UserModel savedUser = userService.registerUser(input, getRequest(), getResponse());

            return UserRegistrationResponse.newBuilder()
                    .user(userMapper.toUser(savedUser))
                    .success(true)
                    .message("User registered successfully")
                    .build();
        } catch (ApplicationException e) {
            return UserRegistrationResponse.newBuilder()
                    .success(false)
                    .message(messageService.getMessage(e))
                    .build();
        } catch (Exception e) {
            log.error("Registration error", e);
            return UserRegistrationResponse.newBuilder()
                    .success(false)
                    .message("Registration failed: " + e.getMessage())
                    .build();
        }
    }

    @DgsMutation
    public Boolean verifyEmailToken(@InputArgument String token) {
        return userService.verifyEmail(token);
    }

    @DgsMutation
    public AuthPayload login(@InputArgument LoginInput input) {
        log.info("Logging user {}", input.getUsername());

        try {
            authenticationService.authenticate(input.getUsername(), input.getPassword(), getRequest(), getResponse());

            UserModel user = userRepository.findOneByUsername(input.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND, "User", input.getUsername()));

            return AuthPayload.newBuilder()
                    .user(userMapper.toUser(user))
                    .success(true)
                    .message("Authentication successful")
                    .build();
        } catch (ApplicationException e) {
            log.error("Authentication error", e);
            return AuthPayload.newBuilder()
                    .success(false)
                    .message(messageService.getMessage(e))
                    .build();
        } catch (BadCredentialsException bce) {
            log.error("Bad credentials", bce);
            return AuthPayload.newBuilder()
                    .success(false)
                    .message(messageService.getMessage(ErrorCode.INVALID_CREDENTIALS))
                    .build();
        } catch (Exception e) {
            log.error("Authentication error", e);
            return AuthPayload.newBuilder()
                    .success(false)
                    .message(e.getMessage())
                    .build();
        }
    }

    @DgsMutation
    public AuthPayload refreshToken() {
        try {
            authenticationService.refreshToken(getRequest(), getResponse());

            String username = getRequest().getUserPrincipal().getName();
            UserModel user = userRepository.findOneByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND, "User", username));

            return AuthPayload.newBuilder()
                    .user(userMapper.toUser(user))
                    .success(true)
                    .message("Token refreshed successfully")
                    .build();
        } catch (ApplicationException e) {
            log.error("Refresh token error", e);
            return AuthPayload.newBuilder()
                    .success(false)
                    .message(messageService.getMessage(e))
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
                    .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND, "User", username));

            authenticationService.revokeAllSessions(userModel.getId(), getResponse());
            return true;
        } catch (Exception e) {
            log.error("Revoke all sessions error", e);
            return false;
        }
    }

    @DgsMutation
    public Boolean requestPasswordReset(@InputArgument String email) {
        return userService.requestPasswordReset(email);
    }

    @DgsMutation
    public Boolean resetPassword(@InputArgument ResetPasswordInput input) {
        return userService.resetPassword(input.getToken(), input.getNewPassword());
    }
}
