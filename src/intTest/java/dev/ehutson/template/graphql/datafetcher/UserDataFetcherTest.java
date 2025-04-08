package dev.ehutson.template.graphql.datafetcher;

import dev.ehutson.template.codegen.types.*;
import dev.ehutson.template.config.TestContainersConfig;
import dev.ehutson.template.domain.RoleModel;
import dev.ehutson.template.domain.UserModel;
import dev.ehutson.template.exception.InsufficientPrivilegesException;
import dev.ehutson.template.repository.RoleRepository;
import dev.ehutson.template.repository.UserRepository;
import dev.ehutson.template.security.service.AuthorizationService;
import dev.ehutson.template.security.service.UserDetailsImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Integration tests for UserDataFetcher.
 * These tests verify GraphQL operations with different authentication scenarios.
 */
@Testcontainers
@SpringBootTest
@Import(TestContainersConfig.class)
@ActiveProfiles("test")
class UserDataFetcherTest {

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

    private SecurityContext originalSecurityContext;
    private AuthorizationService originalAuthorizationService;

    private static final String ACCESS_DENIED = "Access Denied";

    /**
     * Sets up the test environment before each test.
     * Saves the original security context, clears previous test data,
     * creates test roles and users, and retrieves the original AuthorizationService.
     */
    @BeforeEach
    void setUp() {
        // Save original security context
        originalSecurityContext = SecurityContextHolder.getContext();

        // Clear previous test data
        userRepository.deleteAll();
        roleRepository.deleteAll();

        // Create test roles
        createRoles();

        // Create test users
        createUsers();

        // Get original authorization service
        try {
            java.lang.reflect.Field field = UserDataFetcher.class.getDeclaredField("authorizationService");
            field.setAccessible(true);
            originalAuthorizationService = (AuthorizationService) field.get(userDataFetcher);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get AuthorizationService", e);
        }
    }

    /**
     * Cleans up the test environment after each test.
     * Restores the original security context and AuthorizationService,
     * and clears the test data.
     */
    @AfterEach
    void tearDown() {
        // Restore original security context
        SecurityContextHolder.setContext(originalSecurityContext);

        // Restore original AuthorizationService if it was replaced
        if (originalAuthorizationService != null) {
            try {
                java.lang.reflect.Field field = UserDataFetcher.class.getDeclaredField("authorizationService");
                field.setAccessible(true);
                field.set(userDataFetcher, originalAuthorizationService);
            } catch (Exception e) {
                throw new RuntimeException("Failed to restore AuthorizationService", e);
            }
        }

        userRepository.deleteAll();
        roleRepository.deleteAll();
    }

    /**
     * Tests the getCurrentUser GraphQL operation.
     * Verifies that the current authenticated user is returned correctly.
     */
    @Test
    void testGetCurrentUser() {
        // Set up security context with authenticated user
        authenticateAsUser(testUser);

        // Test the GraphQL operation
        User currentUser = userDataFetcher.getCurrentUser();

        // Verify the returned user
        assertNotNull(currentUser);
        assertEquals(testUser.getId(), currentUser.getId());
        assertEquals(testUser.getUsername(), currentUser.getUsername());
        assertEquals(testUser.getEmail(), currentUser.getEmail());
        assertEquals(testUser.getFirstName(), currentUser.getFirstName());
        assertEquals(testUser.getLastName(), currentUser.getLastName());
        assertEquals(1, currentUser.getRoles().size());
        assertEquals("ROLE_USER", currentUser.getRoles().getFirst().getName());
    }

    /**
     * Tests the getCurrentUser GraphQL operation when not authenticated.
     * Verifies that an AccessDeniedException is thrown.
     */
    @Test
    void testGetCurrentUserWhenNotAuthenticated() {
        // Clear security context
        SecurityContextHolder.clearContext();

        // Test the GraphQL operation - should throw AccessDeniedException
        Exception exception = assertThrows(InsufficientPrivilegesException.class, () -> {
            userDataFetcher.getCurrentUser();
        });

        assertEquals("You do not have sufficient privileges to access this resource", exception.getMessage());
    }

    /**
     * Tests the getUser GraphQL operation as an admin user.
     * Verifies that the user details are returned correctly.
     */
    @Test
    void testGetUserAsAdmin() {
        // Set up security context with admin user
        authenticateAsUser(adminUser);

        // Test the GraphQL operation
        User user = userDataFetcher.getUser(testUser.getId());

        // Verify the returned user
        assertNotNull(user);
        assertEquals(testUser.getId(), user.getId());
        assertEquals(testUser.getUsername(), user.getUsername());
    }

    /**
     * Tests the getUser GraphQL operation as the user themselves.
     * Verifies that the user details are returned correctly.
     */
    @Test
    void testGetUserAsSelf() {
        // Set up security context with regular user
        authenticateAsUser(testUser);

        // Test the GraphQL operation - accessing own profile should work
        User user = userDataFetcher.getUser(testUser.getId());

        // Verify the returned user
        assertNotNull(user);
        assertEquals(testUser.getId(), user.getId());
    }

    /**
     * Tests the getUser GraphQL operation as an unauthorized user.
     * Verifies that an AccessDeniedException is thrown.
     */
    @Test
    void testGetUserAsUnauthorizedUser() {
        // Set up security context with regular user
        authenticateAsUser(testUser);

        String userId = adminUser.getId();
        assertNotNull(userId);

        // Test the GraphQL operation - accessing another user should fail
        Exception exception = assertThrows(AccessDeniedException.class,
                () -> userDataFetcher.getUser(userId)
        );

        assertTrue(exception.getMessage().contains(ACCESS_DENIED));
    }

    /**
     * Tests the getAllUsers GraphQL operation as an admin user.
     * Verifies that all users are returned correctly.
     */
    @Test
    void testGetAllUsersAsAdmin() {
        // Set up security context with admin user
        authenticateAsUser(adminUser);

        // Test the GraphQL operation
        //UserConnection userConnection = userDataFetcher.getAllUsers(10, null, null, null);
        UserConnection userConnection = userDataFetcher.getAllUsers(PaginationInput.newBuilder().first(10).build());

        // Verify the returned connection
        assertNotNull(userConnection);
        assertEquals(2, userConnection.getTotalCount());
        assertEquals(2, userConnection.getEdges().size());
    }

    /**
     * Tests the getAllUsers GraphQL operation as a regular user.
     * Verifies that an AccessDeniedException is thrown.
     */
    @Test
    void testGetAllUsersAsRegularUser() {
        // Set up security context with regular user
        authenticateAsUser(testUser);

        // Test the GraphQL operation - should fail due to authorization
        Exception exception = assertThrows(AccessDeniedException.class, () -> {
            userDataFetcher.getAllUsers(PaginationInput.newBuilder().first(10).build());
            //userDataFetcher.getAllUsers(10, null, null, null);
        });

        assertTrue(exception.getMessage().contains(ACCESS_DENIED));
    }

    /**
     * Tests the getAllRoles GraphQL operation as an admin user.
     * Verifies that all roles are returned correctly.
     */
    @Test
    void testGetAllRolesAsAdmin() {
        // Set up security context with admin user
        authenticateAsUser(adminUser);

        // Test the GraphQL operation
        List<Role> roles = userDataFetcher.getAllRoles();

        // Verify the returned roles
        assertNotNull(roles);
        assertEquals(2, roles.size());
        assertTrue(roles.stream().anyMatch(role -> role.getName().equals("ROLE_USER")));
        assertTrue(roles.stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN")));
    }

    /**
     * Tests the getAllRoles GraphQL operation as a regular user.
     * Verifies that an AccessDeniedException is thrown.
     */
    @Test
    void testGetAllRolesAsRegularUser() {
        // Set up security context with regular user
        authenticateAsUser(testUser);

        // Test the GraphQL operation - should fail due to authorization
        Exception exception = assertThrows(AccessDeniedException.class, () -> {
            userDataFetcher.getAllRoles();
        });

        assertTrue(exception.getMessage().contains(ACCESS_DENIED));
    }

    /**
     * Tests the createUser GraphQL operation as an admin user.
     * Verifies that a new user is created correctly.
     */
    @Test
    void testCreateUserAsAdmin() {
        // Set up security context with admin user
        authenticateAsUser(adminUser);

        // Create test data
        String username = "newuser" + UUID.randomUUID().toString().substring(0, 8);

        CreateUserInput input = CreateUserInput.newBuilder()
                .username(username)
                .email(username + "@example.com")
                .firstName("New")
                .lastName("User")
                .password("password123")
                .roles(List.of(userRole.getName()))
                .langKey("en")
                .build();

        // Test the GraphQL operation
        User createdUser = userDataFetcher.createUser(input);

        // Verify the created user
        assertNotNull(createdUser);
        assertEquals(username, createdUser.getUsername());
        assertEquals(input.getEmail(), createdUser.getEmail());
        assertEquals(input.getFirstName(), createdUser.getFirstName());
        assertEquals(input.getLastName(), createdUser.getLastName());

        // Verify in database
        assertTrue(userRepository.existsByUsername(username));
    }

    /**
     * Tests the createUser GraphQL operation as a regular user.
     * Verifies that an AccessDeniedException is thrown.
     */
    @Test
    void testCreateUserAsRegularUser() {
        // Set up security context with regular user
        authenticateAsUser(testUser);

        // Create test data
        CreateUserInput input = CreateUserInput.newBuilder()
                .username("unauthorizeduser")
                .email("unauthorized@example.com")
                .firstName("Unauthorized")
                .lastName("User")
                .password("password123")
                .roles(List.of(userRole.getName()))
                .langKey("en")
                .build();

        // Test the GraphQL operation - should fail due to authorization
        Exception exception = assertThrows(AccessDeniedException.class, () -> {
            userDataFetcher.createUser(input);
        });

        assertTrue(exception.getMessage().contains(ACCESS_DENIED));

        // Verify user was not created
        assertFalse(userRepository.existsByUsername("unauthorizeduser"));
    }

    /**
     * Tests the updateUser GraphQL operation as an admin user.
     * Verifies that the user details are updated correctly.
     */
    @Test
    void testUpdateUserAsAdmin() {
        // Set up security context with admin user
        authenticateAsUser(adminUser);

        // Create test data
        UpdateUserInput input = UpdateUserInput.newBuilder()
                .firstName("UpdatedFirstName")
                .lastName("UpdatedLastName")
                .build();

        // Test the GraphQL operation
        User updatedUser = userDataFetcher.updateUser(testUser.getId(), input);

        // Verify the updated user
        assertNotNull(updatedUser);
        assertEquals("UpdatedFirstName", updatedUser.getFirstName());
        assertEquals("UpdatedLastName", updatedUser.getLastName());

        // Verify in database
        Optional<UserModel> userInDb = userRepository.findById(testUser.getId());
        assertTrue(userInDb.isPresent());
        assertEquals("UpdatedFirstName", userInDb.get().getFirstName());
        assertEquals("UpdatedLastName", userInDb.get().getLastName());
    }

    /**
     * Tests the updateUser GraphQL operation as the user themselves.
     * Verifies that the user details are updated correctly.
     */
    @Test
    void testUpdateUserAsSelf() {
        // Set up security context with regular user
        authenticateAsUser(testUser);

        // Create test data
        UpdateUserInput input = UpdateUserInput.newBuilder()
                .firstName("SelfUpdatedFirstName")
                .lastName("SelfUpdatedLastName")
                .build();

        // Test the GraphQL operation - user should be able to update self
        User updatedUser = userDataFetcher.updateUser(testUser.getId(), input);

        // Verify the updated user
        assertNotNull(updatedUser);
        assertEquals("SelfUpdatedFirstName", updatedUser.getFirstName());
        assertEquals("SelfUpdatedLastName", updatedUser.getLastName());
    }

    /**
     * Tests the updateUser GraphQL operation as an unauthorized user.
     * Verifies that an AccessDeniedException is thrown.
     */
    @Test
    void testUpdateUserAsUnauthorizedUser() {
        // Set up security context with regular user
        authenticateAsUser(testUser);

        // Create test data
        UpdateUserInput input = UpdateUserInput.newBuilder()
                .firstName("UnauthorizedUpdate")
                .lastName("UnauthorizedUpdate")
                .build();

        String userId = adminUser.getId();
        assertNotNull(userId);

        // Test the GraphQL operation - should fail due to authorization
        Exception exception = assertThrows(AccessDeniedException.class,
                () -> userDataFetcher.updateUser(userId, input)
        );

        assertTrue(exception.getMessage().contains(ACCESS_DENIED));
    }

    /**
     * Tests the deleteUser GraphQL operation as an admin user.
     * Verifies that the user is deleted correctly.
     */
    @Test
    void testDeleteUserAsAdmin() {
        // Set up security context with admin user
        authenticateAsUser(adminUser);

        // Test the GraphQL operation
        boolean result = userDataFetcher.deleteUser(testUser.getId());

        // Verify the result
        assertTrue(result);

        // Verify in database
        assertFalse(userRepository.existsById(testUser.getId()));
    }

    /**
     * Tests the deleteUser GraphQL operation as a regular user.
     * Verifies that an AccessDeniedException is thrown.
     */
    @Test
    void testDeleteUserAsRegularUser() {
        // Set up security context with regular user
        authenticateAsUser(testUser);

        String userId = adminUser.getId();
        assertNotNull(userId);

        // Test the GraphQL operation - should fail due to authorization
        Exception exception = assertThrows(AccessDeniedException.class,
                () -> userDataFetcher.deleteUser(userId)
        );

        System.out.println(exception.getMessage());

        assertTrue(exception.getMessage().contains(ACCESS_DENIED));

        // Verify user was not deleted
        assertTrue(userRepository.existsById(adminUser.getId()));
    }

    /**
     * Tests the createRole GraphQL operation as an admin user.
     * Verifies that a new role is created correctly.
     */
    @Test
    void testCreateRoleAsAdmin() {
        // Set up security context with admin user
        authenticateAsUser(adminUser);

        // Create test data
        CreateRoleInput input = CreateRoleInput.newBuilder()
                .name("ROLE_TEST")
                .description("Test role")
                .build();

        // Test the GraphQL operation
        Role createdRole = userDataFetcher.createRole(input);

        // Verify the created role
        assertNotNull(createdRole);
        assertEquals("ROLE_TEST", createdRole.getName());
        assertEquals("Test role", createdRole.getDescription());

        // Verify in database
        assertTrue(roleRepository.findByName("ROLE_TEST").isPresent());
    }

    /**
     * Tests the assignRoleToUser GraphQL operation as an admin user.
     * Verifies that the role is assigned to the user correctly.
     */
    @Test
    void testAssignRoleToUserAsAdmin() {
        // Set up security context with admin user
        authenticateAsUser(adminUser);

        // Test the GraphQL operation
        User updatedUser = userDataFetcher.assignRoleToUser(testUser.getId(), adminRole.getId());

        // Verify the updated user
        assertNotNull(updatedUser);
        assertEquals(2, updatedUser.getRoles().size());
        assertTrue(updatedUser.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN")));

        // Verify in database
        Optional<UserModel> userInDb = userRepository.findById(testUser.getId());
        assertTrue(userInDb.isPresent());
        assertEquals(2, userInDb.get().getRoles().size());
    }

    /**
     * Authenticates the given user by setting up the security context.
     * Mocks the AuthorizationService to return the test user.
     *
     * @param user the user to authenticate
     */
    private void authenticateAsUser(UserModel user) {
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
    private void createRoles() {
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
    private void createUsers() {
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