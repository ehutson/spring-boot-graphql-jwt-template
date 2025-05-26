package dev.ehutson.template.graphql.datafetcher;

import dev.ehutson.template.domain.RoleModel;
import dev.ehutson.template.domain.UserModel;
import dev.ehutson.template.repository.RoleRepository;
import dev.ehutson.template.repository.UserRepository;
import dev.ehutson.template.security.service.AuthorizationService;
import dev.ehutson.template.security.service.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Component
public class DataFetcherTestUtils {

    @Autowired
    private UserDataFetcher userDataFetcher;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UserModel testUser;
    private UserModel adminUser;
    private RoleModel userRole;
    private RoleModel adminRole;

    UserModel getTestUser() {
        return testUser;
    }

    UserModel getAdminUser() {
        return adminUser;
    }

    RoleModel getUserRole() {
        return userRole;
    }

    RoleModel getAdminRole() {
        return adminRole;
    }

    void initializeTestData() {
        // Clear previous test data
        userRepository.deleteAll();
        roleRepository.deleteAll();

        // Create test roles
        createRoles();

        // Create test users
        createUsers();
    }

    void resetTestData() {
        // Clear previous test data
        userRepository.deleteAll();
        roleRepository.deleteAll();
    }


    /**
     * Authenticates the given user by setting up the security context.
     * Mocks the AuthorizationService to return the test user.
     *
     * @param user the user to authenticate
     */
    void authenticateAsUser(UserModel user) {
        // Setup Security Context
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        // Create UserDetails
        List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .toList();

        UserDetailsImpl userDetails = UserDetailsImpl.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .authorities(authorities)
                .enabled(true)
                .build();

        // Create Authentication
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        when(securityContext.getAuthentication()).thenReturn(authentication);

        // Mock the AuthorizationService to return our test user
        AuthorizationService authorizationService = mock(AuthorizationService.class);
        when(authorizationService.getCurrentUser()).thenReturn(Optional.of(user));
        when(authorizationService.isResourceOwner(user.getId())).thenReturn(true);

        // For admin user, mock admin role check
        if (user.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN"))) {
            when(authorizationService.hasRole("ROLE_ADMIN")).thenReturn(true);
        } else {
            when(authorizationService.hasRole("ROLE_ADMIN")).thenReturn(false);
        }

        // Setup role assignment methods
        when(authorizationService.assignRoleToUser(testUser.getId(), adminRole.getId())).thenAnswer(invocation -> {
            testUser.getRoles().add(adminRole);
            return testUser;
        });

        when(authorizationService.removeRoleFromUser(testUser.getId(), adminRole.getId())).thenAnswer(invocation -> {
            testUser.getRoles().remove(adminRole);
            return testUser;
        });

        // Use reflection to set the mocked AuthorizationService
        try {
            java.lang.reflect.Field field = UserDataFetcher.class.getDeclaredField("authorizationService");
            field.setAccessible(true);
            field.set(userDataFetcher, authorizationService);
        } catch (Exception e) {
            throw new RuntimeException("Failed to mock AuthorizationService", e);
        }
    }

    /**
     * Creates test roles and saves them in the repository.
     */
    void createRoles() {
        // Create user role
        userRole = new RoleModel();
        userRole.setName("ROLE_USER");
        userRole.setDescription("Regular user role");
        userRole.setPredefined(true);
        userRole = roleRepository.save(userRole);

        // Create admin role
        adminRole = new RoleModel();
        adminRole.setName("ROLE_ADMIN");
        adminRole.setDescription("Administrator role");
        adminRole.setPredefined(true);
        adminRole = roleRepository.save(adminRole);
    }

    /**
     * Creates test users and saves them in the repository.
     */
    void createUsers() {
        // Create regular test user
        testUser = new UserModel();
        testUser.setUsername("testuser");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setLangKey("en");
        testUser.setActivated(true);
        testUser.setRoles(new ArrayList<>(List.of(userRole)));
        testUser = userRepository.save(testUser);

        // Create admin user
        adminUser = new UserModel();
        adminUser.setUsername("adminuser");
        adminUser.setPassword(passwordEncoder.encode("password123"));
        adminUser.setEmail("admin@example.com");
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setLangKey("en");
        adminUser.setActivated(true);
        adminUser.setRoles(new ArrayList<>(List.of(userRole, adminRole)));
        adminUser = userRepository.save(adminUser);
    }
}
