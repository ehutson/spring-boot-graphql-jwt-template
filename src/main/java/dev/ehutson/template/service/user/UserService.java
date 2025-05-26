package dev.ehutson.template.service.user;

import dev.ehutson.template.codegen.types.RegisterInput;
import dev.ehutson.template.domain.RoleModel;
import dev.ehutson.template.domain.UserModel;
import dev.ehutson.template.exception.ApplicationException;
import dev.ehutson.template.exception.ErrorCode;
import dev.ehutson.template.repository.RoleRepository;
import dev.ehutson.template.repository.UserRepository;
import dev.ehutson.template.security.service.AuthenticationService;
import dev.ehutson.template.service.mail.MailService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationService authenticationService;
    private final MailService mailService;

    public UserModel registerUser(RegisterInput input, HttpServletRequest request, HttpServletResponse response) {
        if (userRepository.existsByUsername(input.getUsername())) {
            throw ApplicationException.of(ErrorCode.RESOURCE_ALREADY_EXISTS, "User already exists", "User", "username", input.getUsername());
        }

        if (userRepository.existsByEmail(input.getEmail())) {
            throw ApplicationException.of(ErrorCode.RESOURCE_ALREADY_EXISTS, "Email already exists", "User", "Email Address", input.getEmail());
        }

        UserModel user = new UserModel();
        user.setUsername(input.getUsername());
        user.setEmail(input.getEmail());
        user.setPassword(passwordEncoder.encode(input.getPassword()));
        user.setFirstName(input.getFirstName());
        user.setLastName(input.getLastName());
        user.setLangKey(input.getLangKey());
        user.setTimezone(input.getTimezone());

        List<RoleModel> roles = new ArrayList<>();
        RoleModel userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> ApplicationException.of(ErrorCode.RESOURCE_NOT_FOUND, "Default Role not found", "Default Role", "ROLE_USER"));
        roles.add(userRole);
        user.setRoles(roles);

        user.setActivationKey(UUID.randomUUID().toString());

        UserModel savedUser = userRepository.save(user);

        authenticationService.authenticate(input.getUsername(), input.getPassword(), request, response);

        mailService.sendActivationEmail(savedUser);

        return savedUser;
    }

    public Boolean verifyEmail(String token) {
        Optional<UserModel> user = userRepository.findOneByActivationKey(token);
        if (user.isPresent()) {
            UserModel userModel = user.get();
            Instant deadline = userModel.getActivationDate().plus(4, ChronoUnit.HOURS);

            if (deadline.isBefore(Instant.now())) {
                userModel.setActivated(true);
                userModel.setActivationKey(null);
                userRepository.save(userModel);
                log.debug("User activated: {}", userModel.getUsername());
                return true;
            }
            log.debug("User activation failed.  Activation token expired. {}", userModel.getUsername());
            return false;
        }
        log.debug("User activation failed.  No user found for token {}.", token);
        return false;
    }

    public Boolean requestPasswordReset(String email) {
        Optional<UserModel> user = userRepository.findOneByEmailIgnoreCase(email);
        if (user.isPresent()) {
            UserModel userModel = user.get();
            userModel.setResetKey(UUID.randomUUID().toString());
            userModel.setResetDate(Instant.now());
            userRepository.save(userModel);
            mailService.sendPasswordResetMail(userModel);
            log.debug("Password reset email activated: {}", email);
            return true;
        }
        log.debug("Password Reset Request Failed.  No user found for email {}.", email);
        return false;
    }

    public Boolean resetPassword(String token, String password) {
        Optional<UserModel> user = userRepository.findOneByActivationKey(token);
        if (user.isPresent()) {
            UserModel userModel = user.get();
            Instant deadline = userModel.getResetDate().plus(4, ChronoUnit.HOURS);

            if (deadline.isBefore(Instant.now())) {
                userModel.setResetKey(null);
                userModel.setPassword(passwordEncoder.encode(password));
                userRepository.save(userModel);
                log.debug("Password reset for user {}.", userModel.getUsername());
                return true;
            }

            log.debug("Password reset failed.  Password token expired. {}", token);
            return false;
        }
        log.debug("Password reset failed.  No user found for token {}.", token);
        return false;
    }
}
