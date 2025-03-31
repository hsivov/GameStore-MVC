package org.example.gamestoreapp.service.session;

import org.example.gamestoreapp.model.entity.User;
import org.example.gamestoreapp.model.enums.UserRole;
import org.example.gamestoreapp.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AppUserDetailsService appUserDetailsService;

    @Test
    void testLoadUserByUsername_WhenUserExists() {
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("username");
        mockUser.setPassword("password");
        mockUser.setRole(UserRole.USER);

        when(userRepository.findByUsername("username")).thenReturn(Optional.of(mockUser));

        UserDetails result = appUserDetailsService.loadUserByUsername("username");

        assertNotNull(result);
        assertEquals("username", result.getUsername());
        assertEquals("password", result.getPassword());
        assertFalse(result.getAuthorities().isEmpty());

        verify(userRepository, times(1)).findByUsername("username");
    }

    @Test
    void testLoadUserByUsername_whenUserNotFound() {
        when(userRepository.findByUsername("unknownUser")).thenReturn(Optional.empty());

        Exception exception = assertThrows(UsernameNotFoundException.class,
                () -> appUserDetailsService.loadUserByUsername("unknownUser"));

        assertEquals("User with username unknownUser not found", exception.getMessage());
        verify(userRepository, times(1)).findByUsername("unknownUser");
    }
}