package dev.ehutson.template.graphql.datafetcher;

import dev.ehutson.template.codegen.types.AuthPayload;
import dev.ehutson.template.codegen.types.LoginInput;
import dev.ehutson.template.codegen.types.RegisterInput;
import dev.ehutson.template.config.TestContainersConfig;
import dev.ehutson.template.domain.RoleModel;
import dev.ehutson.template.domain.UserModel;
import dev.ehutson.template.repository.RoleRepository;
import dev.ehutson.template.repository.UserRepository;
import dev.ehutson.template.security.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Integration tests for AuthDataFetcher.
 * <p>
 * This test class verifies the functionality of the AuthDataFetcher which
 * handles GraphQL mutations for user authentication and registration.
 * <p>
 * The tests focus on:
 * 1. User registration (successful and failure cases)
 * 2. User login (successful and failure cases)
 * 3. User logout
 * 4. Error handling for duplicate usernames and emails
 * <p>
 * The AuthenticationService is mocked to isolate the test from authentication
 * dependencies while still testing the actual data fetcher implementation.
 */
@Testcontainers
@SpringBootTest
@Import(TestContainersConfig.class)
@ActiveProfiles("test")
class AuthDataFetcherTest {

    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_INVALID_PASSWORD = "incorrect_password";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_FIRST_NAME = "John";
    private static final String TEST_LAST_NAME = "Smith";
    private static final String TEST_LANG_KEY = "EN";

    @Autowired
    private AuthDataFetcher authDataFetcher;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private AuthenticationService authenticationService;

    private RoleModel userRole;

    MockHttpServletRequest request;
    MockHttpServletResponse response;

    /**
     * Sets up the test environment before each test.
     * This method:
     * 1. Clears previous test data
     * 2. Creates the user role
     * 3. Sets up mock HttpServletRequest and HttpServletResponse
     * 4. Configures mocked AuthenticationService to throw exceptions for invalid credentials
     */
    @BeforeEach
    void setUp() {
        // Clear previous test data
        userRepository.deleteAll();
        roleRepository.deleteAll();

        createUserRole();

        // Mock ServletRequestAttributes
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(request, response);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);

        doThrow(new BadCredentialsException("bad credentials"))
                .when(authenticationService)
                .authenticate(
                        eq(TEST_USERNAME),
                        eq(TEST_INVALID_PASSWORD),
                        any(HttpServletRequest.class),
                        any(HttpServletResponse.class)
                );
    }


    /**
     * Cleans up the test environment after each test.
     * This method:
     * 1. Clears any user and role data
     * 2. Resets the ServletRequestAttributes
     */
    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
        roleRepository.deleteAll();
        RequestContextHolder.resetRequestAttributes();
    }

    /**
     * Tests the register GraphQL mutation for a successful registration.
     * This test verifies that:
     * 1. The user is successfully registered
     * 2. The response contains the correct user details
     * 3. The user is saved in the database with the correct properties
     * 4. The authentication service is called with the correct credentials
     */
    @Test
    void testRegister_Success() {
        // Prepare test data
        RegisterInput input = new RegisterInput();
        input.setUsername(TEST_USERNAME);
        input.setPassword(TEST_PASSWORD);
        input.setEmail(TEST_EMAIL);
        input.setFirstName(TEST_FIRST_NAME);
        input.setLastName(TEST_LAST_NAME);
        input.setLangKey(TEST_LANG_KEY);

        // Call the method under test
        AuthPayload result = authDataFetcher.register(input);

        assertTrue(result.getSuccess());
        assertNotNull(result.getUser());
        assertEquals(TEST_USERNAME, result.getUser().getUsername());
        assertEquals(TEST_EMAIL, result.getUser().getEmail());
        assertEquals(TEST_FIRST_NAME, result.getUser().getFirstName());
        assertEquals(TEST_LAST_NAME, result.getUser().getLastName());
        assertEquals(TEST_LANG_KEY, result.getUser().getLangKey());

        // verify the user was saved with the correct role
        UserModel savedUser = userRepository.findOneByUsername(TEST_USERNAME).orElse(null);
        assertNotNull(savedUser);
        assertTrue(savedUser.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_USER")));
        assertTrue(passwordEncoder.matches(TEST_PASSWORD, savedUser.getPassword()));

        // verify that authentication service was called
        verify(authenticationService).authenticate(eq(TEST_USERNAME), eq(TEST_PASSWORD), any(HttpServletRequest.class), any(HttpServletResponse.class));
    }

    /**
     * Tests the register GraphQL mutation when the username already exists.
     * This test verifies that:
     * 1. The registration fails with an appropriate error message
     * 2. No user is returned in the response
     * 3. No new user is created in the database
     * 4. The authentication service is not called
     */
    @Test
    void testRegister_UsernameAlreadyExists() {
        createUser();

        // Prepare test data
        RegisterInput input = new RegisterInput();
        input.setUsername(TEST_USERNAME);
        input.setPassword(TEST_PASSWORD);
        input.setEmail(TEST_EMAIL.concat("2"));
        input.setFirstName(TEST_FIRST_NAME);
        input.setLastName(TEST_LAST_NAME);
        input.setLangKey(TEST_LANG_KEY);

        // Call the method under test
        AuthPayload result = authDataFetcher.register(input);

        // Verify results
        assertFalse(result.getSuccess());
        assertEquals("User with username 'testuser' already exists", result.getMessage());
        assertNull(result.getUser());

        // Verify that no new user was created
        assertEquals(1, userRepository.count());

        // Verify that authentication service was not called
        verify(authenticationService, never()).authenticate(
                any(),
                any(),
                any(HttpServletRequest.class),
                any(HttpServletResponse.class)
        );
    }

    /**
     * Tests the register GraphQL mutation when the email already exists.
     * This test verifies that:
     * 1. The registration fails with an appropriate error message
     * 2. No user is returned in the response
     * 3. No new user is created in the database
     * 4. The authentication service is not called
     */
    @Test
    void testRegister_EmailAlreadyExists() {
        createUser();

        // Prepare test data
        RegisterInput input = new RegisterInput();
        input.setUsername(TEST_USERNAME.concat("2"));
        input.setPassword(TEST_PASSWORD);
        input.setEmail(TEST_EMAIL);
        input.setFirstName(TEST_FIRST_NAME);
        input.setLastName(TEST_LAST_NAME);
        input.setLangKey(TEST_LANG_KEY);

        // Call the method under test
        AuthPayload result = authDataFetcher.register(input);

        // Verify results
        assertFalse(result.getSuccess());
        assertEquals("User with Email Address 'test@example.com' already exists", result.getMessage());
        assertNull(result.getUser());

        // Verify that no new user was created
        assertEquals(1, userRepository.count());

        // Verify that authentication service was not called
        verify(authenticationService, never()).authenticate(
                any(),
                any(),
                any(HttpServletRequest.class),
                any(HttpServletResponse.class)
        );
    }

    /**
     * Tests the login GraphQL mutation for a successful login.
     * This test verifies that:
     * 1. The login is successful
     * 2. The response contains the correct user details
     * 3. The authentication service is called with the correct credentials
     */
    @Test
    void testLogin_Success() {
        createUser();

        // Prepare test data
        LoginInput input = new LoginInput();
        input.setUsername(TEST_USERNAME);
        input.setPassword(TEST_PASSWORD);

        // Call the method under test
        AuthPayload result = authDataFetcher.login(input);

        // Verify results
        assertTrue(result.getSuccess());
        assertNotNull(result.getUser());
        assertEquals(TEST_USERNAME, result.getUser().getUsername());

        // Verify that authentication service was called
        verify(authenticationService).authenticate(
                eq(TEST_USERNAME),
                eq(TEST_PASSWORD),
                any(HttpServletRequest.class),
                any(HttpServletResponse.class)
        );
    }

    /**
     * Tests the login GraphQL mutation for a failed login attempt.
     * This test verifies that:
     * 1. The login fails with an appropriate error message
     * 2. No user is returned in the response
     * 3. The authentication service is called with the incorrect credentials
     */
    @Test
    void testLogin_Failure() {
        createUser();

        // Prepare test data
        LoginInput input = new LoginInput();
        input.setUsername(TEST_USERNAME);
        input.setPassword(TEST_INVALID_PASSWORD);

        // Call the method under test
        AuthPayload result = authDataFetcher.login(input);

        // Verify results
        assertFalse(result.getSuccess());
        assertEquals("Invalid username or password", result.getMessage());
        assertNull(result.getUser());

        // Verify that authentication service was called
        verify(authenticationService).authenticate(
                TEST_USERNAME,
                TEST_INVALID_PASSWORD,
                request,
                response
        );
    }

    /**
     * Tests the logout GraphQL mutation.
     * This test verifies that:
     * 1. The logout is successful
     * 2. The authentication service's logout method is called
     */
    @Test
    void testLogout() {
        createUser();

        boolean result = authDataFetcher.logout();

        assertTrue(result);

        verify(authenticationService).logout(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }

    /**
     * Creates a test user and saves it in the repository.
     * The user is created with the following properties:
     * - Username: testuser
     * - Password: encoded "password123"
     * - Email: test@example.com
     * - Activated: true
     * - Role: ROLE_USER
     */
    private void createUser() {
        UserModel testUser = new UserModel();
        testUser.setUsername(TEST_USERNAME);
        testUser.setPassword(passwordEncoder.encode(TEST_PASSWORD));
        testUser.setEmail(TEST_EMAIL);
        testUser.setActivated(true);

        List<RoleModel> roles = new ArrayList<>();
        roles.add(userRole);
        testUser.setRoles(roles);

        userRepository.save(testUser);
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
}