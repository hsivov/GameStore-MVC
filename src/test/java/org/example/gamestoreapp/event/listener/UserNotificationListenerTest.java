package org.example.gamestoreapp.event.listener;

import org.example.gamestoreapp.event.*;
import org.example.gamestoreapp.model.dto.OrderResponseDTO;
import org.example.gamestoreapp.model.entity.User;
import org.example.gamestoreapp.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserNotificationListenerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private UserNotificationListener userNotificationListener;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
    }

    @Test
    void testOnUserRegistered() {
        UserRegisteredEvent event = new UserRegisteredEvent(mockUser);

        userNotificationListener.onUserRegistered(event);

        verify(notificationService, times(1)).sendNotification(anyString(), eq(mockUser));
    }

    @Test
    void testOnPasswordChanged() {
        PasswordChangedEvent event = new PasswordChangedEvent(mockUser);

        userNotificationListener.onPasswordChanged(event);

        verify(notificationService, times(1)).sendNotification(anyString(), eq(mockUser));
    }

    @Test
    void testOnPasswordReset() {
        PasswordResetEvent event = new PasswordResetEvent(mockUser);

        userNotificationListener.onPasswordReset(event);

        verify(notificationService, times(1)).sendNotification(anyString(), eq(mockUser));
    }

    @Test
    void testOnUserEnabled() {
        UserEnabledEvent event = new UserEnabledEvent(mockUser);

        userNotificationListener.onUserEnabled(event);

        verify(notificationService, times(1)).sendNotification(anyString(), eq(mockUser));
    }

    @Test
    void testOnOrderApproved() {
        OrderResponseDTO order = new OrderResponseDTO();
        OrderApprovedEvent event = new OrderApprovedEvent(mockUser, order);

        userNotificationListener.onOrderApproved(event);

        verify(notificationService, times(1)).sendNotification(anyString(), eq(mockUser));
    }

    @Test
    void testOnOrderRejected() {
        OrderResponseDTO order = new OrderResponseDTO();
        OrderRejectedEvent event = new OrderRejectedEvent(mockUser, order);

        userNotificationListener.onOrderRejected(event);

        verify(notificationService, times(1)).sendNotification(anyString(), eq(mockUser));
    }
}