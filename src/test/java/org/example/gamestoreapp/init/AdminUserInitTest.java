package org.example.gamestoreapp.init;

import org.example.gamestoreapp.model.entity.User;
import org.example.gamestoreapp.model.enums.UserRole;
import org.example.gamestoreapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserInitTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminUserInit adminUserInit;

    private Field adminPasswordField;

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        // Set admin password via reflection (as @Value injection won't work in a unit test)
        adminPasswordField = AdminUserInit.class.getDeclaredField("adminPassword");
        adminPasswordField.setAccessible(true);
        adminPasswordField.set(adminUserInit, "test_password");
    }

    @Test
    void testRun_WhenNoAdminExists_ShouldCreateAdminUser() {
        when(userRepository.count()).thenReturn(0L);
        when(passwordEncoder.encode("test_password")).thenReturn("encoded_password");

        adminUserInit.run();

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedAdmin = userCaptor.getValue();
        assertNotNull(savedAdmin);
        assertEquals("hristo", savedAdmin.getUsername());
        assertEquals("encoded_password", savedAdmin.getPassword());
        assertEquals("hsivov@gmail.com", savedAdmin.getEmail());
        assertEquals(UserRole.ADMIN, savedAdmin.getRole());
        assertTrue(savedAdmin.isEnabled());
    }

    @Test
    void testRun_WhenAdminExists_ShouldSkipCreation() {
        when(userRepository.count()).thenReturn(1L);

        adminUserInit.run();

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testRun_WhenAdminPasswordNotSet_ShouldThrowException() throws IllegalAccessException {
        adminPasswordField.set(adminUserInit, "");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> adminUserInit.run());
        assertEquals("Admin password is not set in application properties!", exception.getMessage());
    }
}