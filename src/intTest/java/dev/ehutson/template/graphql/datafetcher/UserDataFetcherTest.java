package dev.ehutson.template.graphql.datafetcher;

import dev.ehutson.template.codegen.types.*;
import dev.ehutson.template.config.TestContainersConfig;
import dev.ehutson.template.domain.RoleModel;
import dev.ehutson.template.domain.UserModel;
import dev.ehutson.template.exception.InsufficientPrivilegesException;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

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
    private DataFetcherTestUtils utils;

    private SecurityContext originalSecurityContext;
    private AuthorizationService originalAuthorizationService;

    private static final String ACCESS_DENIED = "Access Denied";

    private UserModel testUser;
    private UserModel adminUser;
    private RoleModel userRole;

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
        testUser = utils.getTestUser();
        adminUser = utils.getAdminUser();
        userRole = utils.getUserRole();

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

        utils.resetTestData();
        testUser = null;
        adminUser = null;
        userRole = null;
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

    private void authenticateAsUser(UserModel user) {
        utils.authenticateAsUser(user);
    }
}