package dev.ehutson.template.graphql.datafetcher;

import dev.ehutson.template.IntTestConfiguration;
import dev.ehutson.template.TemplateApplication;
import dev.ehutson.template.TestContainersConfiguration;
import dev.ehutson.template.codegen.types.Session;
import dev.ehutson.template.domain.RefreshTokenModel;
import dev.ehutson.template.domain.RoleModel;
import dev.ehutson.template.domain.UserModel;
import dev.ehutson.template.repository.RefreshTokenRepository;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Integration tests for SessionDataFetcher.
 * <p>
 * This test class verifies the functionality of the SessionDataFetcher
 * which handles GraphQL queries for user sessions (refresh tokens).
 * <p>
 * The tests focus on:
 * 1. Retrieving active user sessions
 * 2. Proper security context setup and authentication
 * 3. Mocking the AuthorizationService for testing
 * <p>
 * The test uses reflection to replace the original AuthorizationService
 * with a mocked version to isolate the test from security dependencies
 * while still testing the actual data fetcher implementation.
 */
@SpringBootTest(classes = TemplateApplication.class)
@Import({TestContainersConfiguration.class, IntTestConfiguration.class})
@ActiveProfiles("test")
class SessionDataFetcherTest {

    @Autowired
    private SessionDataFetcher sessionDataFetcher;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private UserModel testUser;
    private RoleModel userRole;
    private List<RefreshTokenModel> testSessions;

    private SecurityContext originalSecurityContext;
    private AuthorizationService originalAuthorizationService;

    /**
     * Sets up the test environment before each test.
     * This method:
     * 1. Saves the original security context
     * 2. Clears previous test data
     * 3. Creates test roles and users
     * 4. Creates test sessions
     * 5. Sets up security context with authenticated user
     * 6. Mocks the AuthorizationService
     */
    @BeforeEach
    void setUp() {
        // Save original security context
        originalSecurityContext = SecurityContextHolder.getContext();

        // Clear previous test data
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        // Create test role
        createUserRole();

        // Create test user
        createUser();

        // Create test sessions
        createTestSessions();

        // Set up security context with authenticated user
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        // Create UserDetails
        UserDetailsImpl userDetails = UserDetailsImpl.builder()
                .id(testUser.getId())
                .username(testUser.getUsername())
                .email(testUser.getEmail())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                .enabled(true)
                .build();

        // Create Authentication
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        when(securityContext.getAuthentication()).thenReturn(authentication);

        // Mock the AuthorizationService to return our test user
        AuthorizationService authorizationService = mock(AuthorizationService.class);
        when(authorizationService.getCurrentUser()).thenReturn(Optional.of(testUser));

        // Save original AuthorizationService and use reflection to set the mocked one
        try {
            java.lang.reflect.Field field = SessionDataFetcher.class.getDeclaredField("authorizationService");
            field.setAccessible(true);
            originalAuthorizationService = (AuthorizationService) field.get(sessionDataFetcher);
            field.set(sessionDataFetcher, authorizationService);
        } catch (Exception e) {
            throw new RuntimeException("Failed to mock AuthorizationService", e);
        }
    }

    /**
     * Cleans up the test environment after each test.
     * This method:
     * 1. Restores the original security context
     * 2. Restores the original AuthorizationService
     * 3. Cleans up test data
     */
    @AfterEach
    void tearDown() {
        // Restore original security context
        SecurityContextHolder.setContext(originalSecurityContext);

        // Restore original AuthorizationService
        if (originalAuthorizationService != null) {
            try {
                java.lang.reflect.Field field = SessionDataFetcher.class.getDeclaredField("authorizationService");
                field.setAccessible(true);
                field.set(sessionDataFetcher, originalAuthorizationService);
            } catch (Exception e) {
                throw new RuntimeException("Failed to restore AuthorizationService", e);
            }
        }

        // Clean up test data
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
    }

    /**
     * Tests the getActiveSessions GraphQL operation.
     * This test verifies that:
     * 1. The active sessions are returned correctly
     * 2. The correct number of sessions is returned
     * 3. The session properties are populated correctly
     * 4. The returned sessions match the test sessions created in the database
     */
    @Test
    void testGetActiveSessions() {
        // Act
        List<Session> sessions = sessionDataFetcher.getActiveSessions();

        // Assert
        assertNotNull(sessions);
        assertEquals(3, sessions.size());

        // Verify session properties
        for (Session session : sessions) {
            assertNotNull(session.getId());
            assertNotNull(session.getUserAgent());
            assertNotNull(session.getIpAddress());
            assertNotNull(session.getCreatedAt());
            assertNotNull(session.getExpirationDate());

            // Check if the session belongs to our test sessions by id
            assertTrue(testSessions.stream().anyMatch(ts -> ts.getId().equals(session.getId())));
        }
    }

    /**
     * Creates a test user and saves it in the repository.
     * The user is created with the following properties:
     * - Username: testuser
     * - Password: password123
     * - Email: test@example.com
     * - User role: ROLE_USER
     */
    private void createUser() {
        testUser = new UserModel();
        testUser.setUsername("testuser");
        testUser.setPassword("password123");
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setLangKey("en");
        testUser.setActivated(true);

        List<RoleModel> roles = new ArrayList<>();
        roles.add(userRole);
        testUser.setRoles(roles);

        testUser = userRepository.save(testUser);
    }

    /**
     * Creates a test user role and saves it in the repository.
     * The role is created with the following properties:
     * - Name: ROLE_USER
     * - Description: Test Role
     * - Predefined: true
     */
    private void createUserRole() {
        // Create test role if it doesn't exist
        userRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> {
                    RoleModel role = new RoleModel();
                    role.setName("ROLE_USER");
                    role.setDescription("Test Role");
                    role.setPredefined(true);
                    return roleRepository.save(role);
                });
    }

    /**
     * Creates test sessions (refresh tokens) and saves them in the repository.
     * This method creates 3 test sessions with the following properties:
     * - Token: refresh-token-[0-2]
     * - User ID: testUser.getId()
     * - User Agent: Mozilla/5.0 Test Browser [0-2]
     * - IP Address: 127.0.0.[0-2]
     * - Expiration Date: 1 day from now
     * - Created At: current time
     */
    private void createTestSessions() {
        testSessions = new ArrayList<>();

        // Create 3 test sessions
        for (int i = 0; i < 3; i++) {
            RefreshTokenModel session = new RefreshTokenModel();
            session.setToken("refresh-token-" + i);
            session.setUserId(testUser.getId());
            session.setUserAgent("Mozilla/5.0 Test Browser " + i);
            session.setIpAddress("127.0.0." + i);
            session.setExpiresAt(OffsetDateTime.now().plusDays(1).toInstant());
            session.setCreatedAt(OffsetDateTime.now().toInstant());
            session = refreshTokenRepository.save(session);
            testSessions.add(session);
        }
    }
}