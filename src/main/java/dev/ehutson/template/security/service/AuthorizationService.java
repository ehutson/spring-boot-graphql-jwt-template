package dev.ehutson.template.security.service;

import dev.ehutson.template.domain.RoleModel;
import dev.ehutson.template.domain.UserModel;
import dev.ehutson.template.exception.ResourceNotFoundException;
import dev.ehutson.template.repository.RoleRepository;
import dev.ehutson.template.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthorizationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    private static final String USER_NOT_FOUND = "User not found";

    public boolean hasRole(String roleName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null &&
                authentication.getAuthorities().stream()
                        .anyMatch(authority -> authority.getAuthority().equals(roleName));
    }

    public UserModel assignRoleToUser(String userId, String roleId) {
        UserModel userModel = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        USER_NOT_FOUND,
                        "User", userId
                ));

        RoleModel roleModel = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Role not found",
                        "Role", roleId
                ));

        userModel.getRoles().add(roleModel);
        return userRepository.save(userModel);
    }

    public UserModel removeRoleFromUser(String userId, String roleId) {
        UserModel userModel = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        USER_NOT_FOUND,
                        "User", userId
                ));

        RoleModel roleModel = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Role not found",
                        "Role", roleId
                ));

        userModel.getRoles().remove(roleModel);
        return userRepository.save(userModel);
    }

    public List<String> getUserRoles(String username) {
        UserModel user = userRepository.findOneByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        USER_NOT_FOUND,
                        "User", username
                ));

        return user.getRoles().stream()
                .map(RoleModel::getName)
                .toList();
    }

    public boolean isResourceOwner(String resourceOwnerId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getPrincipal() == null) {
            return false;
        }

        Optional<UserModel> currentUser = getCurrentUser();

        return currentUser.map(user -> user.getId().equals(resourceOwnerId)).orElse(false);
    }

    public Optional<UserModel> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getPrincipal() == null) {
            return Optional.empty();
        }

        return userRepository.findOneByUsername(authentication.getName());
    }
}
