package org.example.gamestoreapp.service.session;

import org.example.gamestoreapp.model.entity.User;
import org.example.gamestoreapp.model.enums.UserRole;
import org.example.gamestoreapp.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserHelperServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserHelperService userHelperService;

    @Test
    void testGetUser_WhenUserAuthenticated() {
        // Mock the authenticated user which also implements UserDetails
        User authenticatedUser = new User();
        authenticatedUser.setUsername("username");
        authenticatedUser.setPassword("password");
        authenticatedUser.setRole(UserRole.USER);

        // Mock authentication
        SecurityContextHolder.getContext().setAuthentication(authentication);
        when(authentication.getPrincipal()).thenReturn(authenticatedUser);
        when(userRepository.findByUsername("username")).thenReturn(Optional.of(authenticatedUser));

        User result = userHelperService.getUser();

        assertNotNull(result);
        assertEquals("username", result.getUsername());
        assertEquals("password", result.getPassword());
        assertEquals(UserRole.USER, result.getRole());

        verify(authentication, times(1)).getPrincipal();
        verify(userRepository, times(1)).findByUsername("username");
    }

    @Test
    void testGetUser_whenNotAuthenticated() {
        // Spring Security provides an AnonymousAuthenticationToken,
        // which represents an unauthenticated user with a default "ANONYMOUS" role.
        AnonymousAuthenticationToken anonymousAuthentication = new AnonymousAuthenticationToken(
                UUID.randomUUID().toString(),
                "anonymousUser",
                List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
        );

        SecurityContextHolder.getContext().setAuthentication(anonymousAuthentication);

        User result = userHelperService.getUser();

        boolean isAuthenticated = userHelperService.isAuthenticated();

        assertNull(result);
        assertFalse(isAuthenticated);
    }
}