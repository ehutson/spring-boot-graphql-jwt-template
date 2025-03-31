package dev.ehutson.template.service;

import dev.ehutson.template.codegen.types.RegisterInput;
import dev.ehutson.template.domain.RoleModel;
import dev.ehutson.template.domain.UserModel;
import dev.ehutson.template.exception.ResourceAlreadyExistsException;
import dev.ehutson.template.exception.ResourceNotFoundException;
import dev.ehutson.template.repository.RoleRepository;
import dev.ehutson.template.repository.UserRepository;
import dev.ehutson.template.security.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
            throw new ResourceAlreadyExistsException("User already exists", "User", "username", input.getUsername());
        }

        if (userRepository.existsByEmail(input.getEmail())) {
            throw new ResourceAlreadyExistsException("Email already exists", "User", "Email Address", input.getEmail());
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
                .orElseThrow(() -> new ResourceNotFoundException("Default Role not found", "Default Role", "ROLE_USER"));
        roles.add(userRole);
        user.setRoles(roles);

        UserModel savedUser = userRepository.save(user);

        authenticationService.authenticate(input.getUsername(), input.getPassword(), request, response);

        return savedUser;
    }

    public Optional<UserModel> getUserByUsername(String username) {
        return userRepository.findOneByUsername(username);
    }
}
