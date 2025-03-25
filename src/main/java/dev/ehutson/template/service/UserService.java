package dev.ehutson.template.service;

import dev.ehutson.template.codegen.types.RegisterInput;
import dev.ehutson.template.domain.RoleModel;
import dev.ehutson.template.domain.UserModel;
import dev.ehutson.template.exception.CustomException;
import dev.ehutson.template.repository.RoleRepository;
import dev.ehutson.template.repository.UserRepository;
import dev.ehutson.template.security.service.AuthenticationService;
import graphql.ErrorType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationService authenticationService;

    public UserModel registerUser(RegisterInput input, HttpServletRequest request, HttpServletResponse response) {
        if (userRepository.existsByUsername(input.getUsername())) {
            throw new CustomException("User already exists", "USERNAME_EXISTS", ErrorType.ValidationError);
        }

        if (userRepository.existsByEmail(input.getEmail())) {
            throw new CustomException("Email is already in use", "EMAIL_EXISTS", ErrorType.ValidationError);
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
                .orElseThrow(() -> new CustomException("Default role not found", "ROLE_NOT_FOUND", ErrorType.DataFetchingException));
        roles.add(userRole);
        user.setRoles(roles);

        UserModel savedUser = userRepository.save(user);

        authenticationService.authenticate(input.getUsername(), input.getPassword(), request, response);

        return savedUser;
    }
}
