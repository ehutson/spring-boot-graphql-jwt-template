package dev.ehutson.template.graphql.datafetcher;

import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import dev.ehutson.template.TestContainersConfiguration;
import dev.ehutson.template.codegen.types.AuthPayload;
import dev.ehutson.template.codegen.types.LoginInput;
import dev.ehutson.template.codegen.types.RegisterInput;
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

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
@Import(TestContainersConfiguration.class)
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

    @MockitoBean
    private DgsDataFetchingEnvironment dfe;

    private RoleModel userRole;

    MockHttpServletRequest request;
    MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        // Clear previous test data
        userRepository.deleteAll();
        roleRepository.deleteAll();

        createUserRole();
        //createUser();

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


    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
        roleRepository.deleteAll();
        RequestContextHolder.resetRequestAttributes();
    }

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
        assertEquals("Username already exists", result.getMessage());
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
        assertEquals("Email is already in use", result.getMessage());
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

    @Test
    void testLogin_Success() {
        createUser();

        // Prepare test data
        LoginInput input = new LoginInput();
        input.setUsername(TEST_USERNAME);
        input.setPassword(TEST_PASSWORD);

        // Call the method under test
        AuthPayload result = authDataFetcher.login(input, dfe);

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

    @Test
    void testLogin_Failure() {
        createUser();

        // Prepare test data
        LoginInput input = new LoginInput();
        input.setUsername(TEST_USERNAME);
        input.setPassword(TEST_INVALID_PASSWORD);

        // Call the method under test
        AuthPayload result = authDataFetcher.login(input, dfe);

        // Verify results
        assertFalse(result.getSuccess());
        assertEquals("Invalid username or password", result.getMessage());
        assertNull(result.getUser());

        // Verify that authentication service was called
        verify(authenticationService).authenticate(
                eq(TEST_USERNAME),
                eq(TEST_INVALID_PASSWORD),
                eq(request),
                eq(response)
        );
    }

    @Test
    void testLogout() {
        createUser();

        boolean result = authDataFetcher.logout();

        assertTrue(result);

        verify(authenticationService).logout(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }

    private void createUser() {
        UserModel testUser = new UserModel();
        testUser.setUsername(TEST_USERNAME);
        testUser.setPassword(passwordEncoder.encode(TEST_PASSWORD));
        testUser.setEmail(TEST_EMAIL);
        testUser.setActivated(true);

        List<RoleModel> roles = new ArrayList<>();
        roles.add(userRole);
        testUser.setRoles(roles);

        testUser = userRepository.save(testUser);
    }

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