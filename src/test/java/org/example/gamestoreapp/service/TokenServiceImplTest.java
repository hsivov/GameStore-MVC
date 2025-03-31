package org.example.gamestoreapp.service;

import org.example.gamestoreapp.exception.IllegalTokenException;
import org.example.gamestoreapp.exception.TokenExpiredException;
import org.example.gamestoreapp.exception.UsedTokenException;
import org.example.gamestoreapp.model.entity.ConfirmationToken;
import org.example.gamestoreapp.model.entity.User;
import org.example.gamestoreapp.repository.ConfirmationTokenRepository;
import org.example.gamestoreapp.service.impl.TokenServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenServiceImplTest {

    @Mock
    private ConfirmationTokenRepository tokenRepository;

    @InjectMocks
    private TokenServiceImpl tokenService;

    private final String token = "validToken123";
    private ConfirmationToken mockConfirmationToken;

    @BeforeEach
    void setUp() {
        User mockUser = new User();
        mockUser.setId(1L);

        mockConfirmationToken = new ConfirmationToken(mockUser);
        mockConfirmationToken.setToken(token);
        mockConfirmationToken.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        mockConfirmationToken.setConfirmedAt(null);
    }

    @Test
    void testSaveConfirmationToken() {
        tokenService.saveConfirmationToken(mockConfirmationToken);

        verify(tokenRepository, times(1)).save(any(ConfirmationToken.class));
    }

    @Test
    void testGetToken() {
        tokenService.getToken(token);

        verify(tokenRepository, times(1)).findByToken("validToken123");
    }

    @Test
    void testVerifyToken_Success() {
        when(tokenRepository.findByToken(token)).thenReturn(Optional.of(mockConfirmationToken));

        assertDoesNotThrow(() -> tokenService.verifyToken(token));
    }

    @Test
    void testVerifyToken_TokenInvalid() {
        when(tokenRepository.findByToken(token)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalTokenException.class, () -> tokenService.verifyToken(token));

        assertEquals("The provided token is invalid.", exception.getMessage());

        verify(tokenRepository, times(1)).findByToken(token);
    }

    @Test
    void testVerifyToken_TokenAlreadyUsed() {
        // Mock used token
        String usedToken = "usedToken";
        mockConfirmationToken.setToken(usedToken);
        mockConfirmationToken.setConfirmedAt(LocalDateTime.now().minusHours(1));

        when(tokenRepository.findByToken(usedToken)).thenReturn(Optional.of(mockConfirmationToken));

        Exception exception = assertThrows(UsedTokenException.class, () -> tokenService.verifyToken(usedToken));

        assertEquals("The token has already been used.", exception.getMessage());

        verify(tokenRepository, times(1)).findByToken(usedToken);
    }

    @Test
    void testVerifyToken_TokenExpired() {
        // Mock expired token
        String expiredToken = "usedToken";
        mockConfirmationToken.setToken(expiredToken);
        mockConfirmationToken.setExpiresAt(LocalDateTime.now().minusMinutes(10));

        when(tokenRepository.findByToken(expiredToken)).thenReturn(Optional.of(mockConfirmationToken));

        Exception exception = assertThrows(TokenExpiredException.class, () -> tokenService.verifyToken(expiredToken));

        assertEquals("The token has expired. Please request a new one.", exception.getMessage());

        verify(tokenRepository, times(1)).findByToken(expiredToken);
    }

    @Test
    void invalidateToken() {
        tokenService.invalidateToken(mockConfirmationToken);

        assertNotNull(mockConfirmationToken.getConfirmedAt());

        verify(tokenRepository, times(1)).save(mockConfirmationToken);
    }
}