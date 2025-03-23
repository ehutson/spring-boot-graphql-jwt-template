package dev.ehutson.template.security.service;

import dev.ehutson.template.domain.RoleModel;
import dev.ehutson.template.domain.UserModel;
import dev.ehutson.template.repository.RoleRepository;
import dev.ehutson.template.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthorizationServiceTest {


    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthorizationService authorizationService;

    @BeforeEach
    void setUp() {
        try (var ignored = MockitoAnnotations.openMocks(this)) {
            SecurityContextHolder.setContext(securityContext);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testAssignRoleToUser() {
        UserModel user = new UserModel();
        user.setId("user1");
        RoleModel role = new RoleModel();
        role.setId("role1");

        when(userRepository.findById("user1")).thenReturn(Optional.of(user));
        when(roleRepository.findById("role1")).thenReturn(Optional.of(role));
        when(userRepository.save(user)).thenReturn(user);

        UserModel result = authorizationService.assignRoleToUser("user1", "role1");

        assertNotNull(result);
        assertTrue(result.getRoles().contains(role));
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testRemoveRoleFromUser() {
        UserModel user = new UserModel();
        user.setId("user1");
        RoleModel role = new RoleModel();
        role.setId("role1");
        user.getRoles().add(role);

        when(userRepository.findById("user1")).thenReturn(Optional.of(user));
        when(roleRepository.findById("role1")).thenReturn(Optional.of(role));
        when(userRepository.save(user)).thenReturn(user);

        UserModel result = authorizationService.removeRoleFromUser("user1", "role1");

        assertNotNull(result);
        assertFalse(result.getRoles().contains(role));
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testIsResourceOwner() {
        UserModel user = new UserModel();
        user.setId("user1");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("user1");
        when(authentication.getPrincipal()).thenReturn(user);
        when(userRepository.findOneByUsername("user1")).thenReturn(Optional.of(user));

        boolean result = authorizationService.isResourceOwner("user1");

        assertTrue(result);
    }

    @Test
    void testGetCurrentUser() {
        UserModel user = new UserModel();
        user.setId("user1");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("user1");
        when(authentication.getPrincipal()).thenReturn(user);
        when(userRepository.findOneByUsername("user1")).thenReturn(Optional.of(user));


        Optional<UserModel> result = authorizationService.getCurrentUser();

        assertTrue(result.isPresent());
        assertEquals("user1", result.get().getId());
    }
}