package org.example.gamestoreapp.event.listener;

import org.example.gamestoreapp.event.OrderApprovedEvent;
import org.example.gamestoreapp.event.OrderRejectedEvent;
import org.example.gamestoreapp.event.PasswordResetRequestEvent;
import org.example.gamestoreapp.event.UserRegisteredEvent;
import org.example.gamestoreapp.model.dto.OrderItemDTO;
import org.example.gamestoreapp.model.dto.OrderResponseDTO;
import org.example.gamestoreapp.model.entity.ConfirmationToken;
import org.example.gamestoreapp.model.entity.User;
import org.example.gamestoreapp.service.EmailService;
import org.example.gamestoreapp.service.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailConfirmationListenerTest {

    @Mock
    private TokenService tokenService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private EmailConfirmationListener emailConfirmationListener;

    private User mockUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailConfirmationListener, "domain", "https://mock-domain.com");

        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");
    }

    @Test
    void testHandleUserRegistered() {
        UserRegisteredEvent event = new UserRegisteredEvent(mockUser);

        emailConfirmationListener.handleUserRegistered(event);

        verify(tokenService, times(1)).saveConfirmationToken(any(ConfirmationToken.class));
        verify(emailService, times(1)).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void testHandlePasswordResetRequest() {
        PasswordResetRequestEvent event = new PasswordResetRequestEvent(mockUser);

        emailConfirmationListener.handlePasswordResetRequested(event);

        verify(tokenService, times(1)).saveConfirmationToken(any(ConfirmationToken.class));
        verify(emailService, times(1)).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void testHandleOrderApproved() {
        OrderResponseDTO order = new OrderResponseDTO();
        order.setBoughtGames(List.of(new OrderItemDTO()));

        OrderApprovedEvent event = new OrderApprovedEvent(mockUser, order);

        emailConfirmationListener.handleOrderApproved(event);

        verify(emailService, times(1)).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void testHandleOrderRejected() {
        OrderResponseDTO order = new OrderResponseDTO();
        order.setBoughtGames(List.of(new OrderItemDTO()));

        OrderRejectedEvent event = new OrderRejectedEvent(mockUser, order);

        emailConfirmationListener.handleOrderRejected(event);

        verify(emailService, times(1)).sendEmail(anyString(), anyString(), anyString());
    }
}