package dev.ehutson.template.graphql.datafetcher;

import dev.ehutson.template.codegen.types.CreateRoleInput;
import dev.ehutson.template.codegen.types.Role;
import dev.ehutson.template.codegen.types.User;
import dev.ehutson.template.config.TestContainersConfig;
import dev.ehutson.template.domain.UserModel;
import dev.ehutson.template.repository.RoleRepository;
import dev.ehutson.template.repository.UserRepository;
import dev.ehutson.template.security.service.AuthorizationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for UserDataFetcher.
 * These tests verify GraphQL operations with different authentication scenarios.
 */
@Testcontainers
@SpringBootTest
@Import(TestContainersConfig.class)
@ActiveProfiles("test")
class RoleDataFetcherTest {

    @Autowired
    private RoleDataFetcher roleDataFetcher;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private DataFetcherTestUtils utils;

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

        utils.initializeTestData();

        // Get original authorization service
        try {
            java.lang.reflect.Field field = UserDataFetcher.class.getDeclaredField("authorizationService");
            field.setAccessible(true);
            originalAuthorizationService = (AuthorizationService) field.get(roleDataFetcher);
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
                field.set(roleDataFetcher, originalAuthorizationService);
            } catch (Exception e) {
                throw new RuntimeException("Failed to restore AuthorizationService", e);
            }
        }

        utils.resetTestData();
    }

    /**
     * Tests the getAllRoles GraphQL operation as an admin user.
     * Verifies that all roles are returned correctly.
     */
    @Test
    void testGetAllRolesAsAdmin() {
        // Set up security context with admin user
        utils.authenticateAsUser(utils.getAdminUser());

        // Test the GraphQL operation
        List<Role> roles = roleDataFetcher.getAllRoles();

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
        utils.authenticateAsUser(utils.getTestUser());

        // Test the GraphQL operation - should fail due to authorization
        Exception exception = assertThrows(AccessDeniedException.class, () -> {
            roleDataFetcher.getAllRoles();
        });

        assertTrue(exception.getMessage().contains(ACCESS_DENIED));
    }

    /**
     * Tests the createRole GraphQL operation as an admin user.
     * Verifies that a new role is created correctly.
     */
    @Test
    void testCreateRoleAsAdmin() {
        // Set up security context with admin user
        utils.authenticateAsUser(utils.getAdminUser());

        // Create test data
        CreateRoleInput input = CreateRoleInput.newBuilder()
                .name("ROLE_TEST")
                .description("Test role")
                .build();

        // Test the GraphQL operation
        Role createdRole = roleDataFetcher.createRole(input);

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
        utils.authenticateAsUser(utils.getAdminUser());

        // Test the GraphQL operation
        User updatedUser = roleDataFetcher.assignRoleToUser(utils.getTestUser().getId(), utils.getAdminRole().getId());

        // Verify the updated user
        assertNotNull(updatedUser);
        assertEquals(2, updatedUser.getRoles().size());
        assertTrue(updatedUser.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN")));

        // Verify in database
        Optional<UserModel> userInDb = userRepository.findById(utils.getTestUser().getId());
        assertTrue(userInDb.isPresent());
        assertEquals(2, userInDb.get().getRoles().size());
    }
}