package org.example.gamestoreapp.service;

import org.example.gamestoreapp.event.*;
import org.example.gamestoreapp.exception.IllegalTokenException;
import org.example.gamestoreapp.exception.UsedTokenException;
import org.example.gamestoreapp.exception.UserNotFoundException;
import org.example.gamestoreapp.model.dto.ChangePasswordBindingModel;
import org.example.gamestoreapp.model.dto.ResetPasswordDTO;
import org.example.gamestoreapp.model.dto.UserRegisterBindingModel;
import org.example.gamestoreapp.model.entity.ConfirmationToken;
import org.example.gamestoreapp.model.entity.User;
import org.example.gamestoreapp.repository.UserRepository;
import org.example.gamestoreapp.service.impl.AuthServiceImpl;
import org.example.gamestoreapp.service.session.UserHelperService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {

    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserHelperService userHelperService;
    @Mock
    private TokenService tokenService;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private AuthServiceImpl authService;

    private User mockUser;
    private UserRegisterBindingModel userRegisterBindingModel;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setUsername("testuser");
        mockUser.setEmail("test@example.com");
        mockUser.setFirstName("Test");
        mockUser.setLastName("User");
        mockUser.setAge(25);
        mockUser.setPassword("hashedPassword");

        userRegisterBindingModel = new UserRegisterBindingModel();
        userRegisterBindingModel.setUsername("testuser");
        userRegisterBindingModel.setEmail("test@example.com");
        userRegisterBindingModel.setFirstName("Test");
        userRegisterBindingModel.setLastName("User");
        userRegisterBindingModel.setAge(25);
        userRegisterBindingModel.setPassword("password123");
    }

    @Test
    void register_ShouldReturnTrue_WhenUserIsSuccessfullyRegistered() {
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        boolean result = authService.register(userRegisterBindingModel);

        assertTrue(result);
        verify(passwordEncoder, times(1)).encode(anyString());
        verify(userRepository, times(1)).save(any(User.class));
        verify(eventPublisher, times(1)).publishEvent(any(UserRegisteredEvent.class));
    }

    @Test
    void register_ShouldReturnFalse_WhenDatabaseErrorOccurs() {
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        doThrow(new DataAccessException("Database error") {}).when(userRepository).save(any(User.class));

        boolean result = authService.register(userRegisterBindingModel);

        assertFalse(result);
        verify(userRepository, times(1)).save(any(User.class));
        verify(eventPublisher, never()).publishEvent(any(UserRegisteredEvent.class));
    }

    @Test
    void register_ShouldReturnFalse_WhenUnexpectedErrorOccurs() {
        when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("Unexpected error"));

        boolean result = authService.register(userRegisterBindingModel);

        assertFalse(result);
        verify(userRepository, times(1)).save(any(User.class));
        verify(eventPublisher, never()).publishEvent(any(UserRegisteredEvent.class));
    }

    @Test
    void testIsCorrectPassword_ShouldReturnTrue_WhenPasswordMatches() {
        when(userHelperService.getUser()).thenReturn(mockUser);
        when(passwordEncoder.matches("enteredPassword", "hashedPassword")).thenReturn(true);

        boolean result = authService.isCorrectPassword("enteredPassword");

        assertTrue(result);
        verify(userHelperService, times(1)).getUser();
        verify(passwordEncoder, times(1)).matches("enteredPassword", "hashedPassword");
    }

    @Test
    void testIsCorrectPassword_ShouldReturnFalse_WhenPasswordDoesNotMatch() {
        when(userHelperService.getUser()).thenReturn(mockUser);
        when(passwordEncoder.matches("wrongPassword", "hashedPassword")).thenReturn(false);

        boolean result = authService.isCorrectPassword("wrongPassword");

        assertFalse(result);
        verify(userHelperService).getUser();
        verify(passwordEncoder).matches("wrongPassword", "hashedPassword");
    }

    @Test
    void testChangePassword() {
        when(userHelperService.getUser()).thenReturn(mockUser);
        when(passwordEncoder.encode("newPassword123")).thenReturn("newHashedPassword");

        ChangePasswordBindingModel changePasswordBindingModel = new ChangePasswordBindingModel();
        changePasswordBindingModel.setNewPassword("newPassword123");

        authService.changePassword(changePasswordBindingModel);

        assertEquals("newHashedPassword", mockUser.getPassword()); // Ensure password was updated
        verify(userHelperService, times(1)).getUser();
        verify(passwordEncoder, times(1)).encode("newPassword123");
        verify(userRepository, times(1)).save(mockUser);
        verify(eventPublisher, times(1)).publishEvent(any(PasswordChangedEvent.class));
    }

    @Test
    void testIsValidEmail_ShouldReturnTrue_WhenEmailExists() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));

        boolean result = authService.isValidEmail("test@example.com");

        assertTrue(result);
        verify(userRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    void testIsValidEmail_ShouldReturnFalse_WhenEmailNotExists() {
        when(userRepository.findByEmail("not_existing@example.com")).thenReturn(Optional.empty());

        boolean result = authService.isValidEmail("not_existing@example.com");

        assertFalse(result);
        verify(userRepository, times(1)).findByEmail("not_existing@example.com");
    }

    @Test
    void passwordResetRequest_ShouldSendEmail_WhenUserExists() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));

        authService.passwordResetRequest("test@example.com");

        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(eventPublisher, times(1)).publishEvent(any(PasswordResetRequestEvent.class));
    }

    @Test
    void passwordResetRequest_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> authService.passwordResetRequest("nonexistent@example.com"));

        verify(userRepository, times(1)).findByEmail("nonexistent@example.com");
        verify(eventPublisher, never()).publishEvent(any(PasswordResetRequestEvent.class));
    }

    @Test
    void testResetPassword_ShouldUpdatePasswordAndInvalidateToken_WhenTokenIsValid() {
        ConfirmationToken confirmationToken = new ConfirmationToken(mockUser);
        confirmationToken.setToken("validToken123");

        ResetPasswordDTO resetPasswordDTO = new ResetPasswordDTO();
        resetPasswordDTO.setConfirmPassword("newPassword123");

        when(tokenService.getToken("validToken123")).thenReturn(Optional.of(confirmationToken));
        when(passwordEncoder.encode("newPassword123")).thenReturn("newEncodedPassword");

        authService.resetPassword(resetPasswordDTO, "validToken123");

        assertEquals("newEncodedPassword", mockUser.getPassword());
        verify(tokenService, times(1)).getToken("validToken123");
        verify(passwordEncoder, times(1)).encode("newPassword123");
        verify(userRepository, times(1)).save(any(User.class));
        verify(tokenService, times(1)).invalidateToken(confirmationToken);
        verify(eventPublisher, times(1)).publishEvent(any(PasswordResetEvent.class));
    }

    @Test
    void testResetPassword_ShouldDoNothing_WhenTokenIsInvalid() {
        ResetPasswordDTO resetPasswordDTO = new ResetPasswordDTO();
        resetPasswordDTO.setConfirmPassword("newPassword123");

        when(tokenService.getToken("invalidToken")).thenReturn(Optional.empty());

        authService.resetPassword(resetPasswordDTO, "invalidToken");

        verify(tokenService, times(1)).getToken("invalidToken");
        verifyNoInteractions(passwordEncoder, userRepository);
    }

    @Test
    void testIsUniqueEmail_ShouldReturnTrue_WhenEmailIsUnique() {
        when(userRepository.findByEmail("unique_email@test.com")).thenReturn(Optional.empty());

        boolean result = authService.isUniqueEmail("unique_email@test.com");

        assertTrue(result);
        verify(userRepository, times(1)).findByEmail("unique_email@test.com");
    }

    @Test
    void testIsUniqueEmail_ShouldReturnFalse_WhenEmailIsNotUnique() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));

        boolean result = authService.isUniqueEmail("test@example.com");

        assertFalse(result);
        verify(userRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    void testIsUniqueUsername_ShouldReturnTrue_WhenUsernameIsUnique() {
        when(userRepository.findByUsername("unique")).thenReturn(Optional.empty());

        boolean result = authService.isUniqueUsername("unique");

        assertTrue(result);
        verify(userRepository, times(1)).findByUsername("unique");
    }

    @Test
    void testIsUniqueUsername_ShouldReturnFalse_WhenUsernameIsNotUnique() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));

        boolean result = authService.isUniqueUsername("testuser");

        assertFalse(result);
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    void testResendConfirmationToken() {
        ConfirmationToken mockToken = new ConfirmationToken(mockUser);
        mockToken.setToken("validToken123");

        when(tokenService.getToken("validToken123")).thenReturn(Optional.of(mockToken));

        authService.resendConfirmationToken("validToken123");

        verify(tokenService, times(1)).getToken("validToken123");
        verify(eventPublisher, times(1)).publishEvent(any(UserRegisteredEvent.class));
    }

    @Test
    void testResendConfirmationToken_ShouldThrow_WhenTokenIsInvalid() {
        when(tokenService.getToken("invalidToken")).thenReturn(Optional.empty());

        assertThrows(IllegalTokenException.class, () -> authService.resendConfirmationToken("invalidToken"));

        verify(tokenService, times(1)).getToken("invalidToken");
        verify(eventPublisher, never()).publishEvent(any(UserRegisteredEvent.class));
    }

    @Test
    void testResendConfirmationToken_ShouldThrow_WhenUserIsAlreadyConfirmed() {
        ConfirmationToken mockToken = new ConfirmationToken(mockUser);
        mockToken.setToken("usedToken123");

        when(tokenService.getToken("usedToken123")).thenReturn(Optional.of(mockToken));

        mockUser.setEnabled(true);

        assertThrows(UsedTokenException.class, () -> authService.resendConfirmationToken("usedToken123"));

        verify(tokenService, times(1)).getToken("usedToken123");
        verify(eventPublisher, never()).publishEvent(any(UserRegisteredEvent.class));
    }

    @Test
    void testEnableUser_WhenTokenExist() {
        ConfirmationToken mockToken = new ConfirmationToken(mockUser);
        mockToken.setToken("validToken123");

        when(tokenService.getToken("validToken123")).thenReturn(Optional.of(mockToken));

        authService.enableUser("validToken123");

        assertTrue(mockUser.isEnabled());
        verify(tokenService, times(1)).getToken("validToken123");
        verify(userRepository, times(1)).save(any(User.class));
        verify(tokenService, times(1)).invalidateToken(mockToken);
        verify(eventPublisher, times(1)).publishEvent(any(UserEnabledEvent.class));
    }

    @Test
    void testEnableUser_WhenTokenNotExist() {
        when(tokenService.getToken("invalidToken")).thenReturn(Optional.empty());

        authService.enableUser("invalidToken");

        assertFalse(mockUser.isEnabled());
        verify(tokenService, times(1)).getToken("invalidToken");
        verify(userRepository, never()).save(any(User.class));
        verify(tokenService, never()).invalidateToken(any(ConfirmationToken.class));
    }
}
