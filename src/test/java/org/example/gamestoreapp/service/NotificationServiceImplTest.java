package org.example.gamestoreapp.service;

import org.example.gamestoreapp.model.dto.NotificationDTO;
import org.example.gamestoreapp.model.entity.Notification;
import org.example.gamestoreapp.model.entity.User;
import org.example.gamestoreapp.repository.NotificationRepository;
import org.example.gamestoreapp.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {
    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private User mockUser;
    private Notification mockNotification;
    private DateTimeFormatter formatter;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);

        mockNotification = new Notification();
        mockNotification.setUser(mockUser);
        mockNotification.setMessage("New game available!");
        mockNotification.setUnread(true);
        mockNotification.setCreatedAt(LocalDateTime.of(2024, 4, 3, 15, 30, 0));

        formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    }

    @Test
    void testUserGetNotifications() {
        when(notificationRepository.findAllByUser(mockUser)).thenReturn(Optional.of(List.of(mockNotification)));

        List<NotificationDTO> result = notificationService.getUserNotifications(mockUser);

        assertEquals(1, result.size());
        assertEquals(mockNotification.getMessage(), result.get(0).getMessage());
        assertEquals(formatter.format(mockNotification.getCreatedAt()), result.get(0).getCreatedAt());
        assertEquals(mockNotification.unread(), result.get(0).isUnread());

        verify(notificationRepository, times(1)).findAllByUser(mockUser);
    }

    @Test
    void testUserGetNotifications_WhenNoNotifications() {
        when(notificationRepository.findAllByUser(mockUser)).thenReturn(Optional.empty());

        List<NotificationDTO> result = notificationService.getUserNotifications(mockUser);

        assertTrue(result.isEmpty());

        verify(notificationRepository, times(1)).findAllByUser(mockUser);
    }

    @Test
    void testCountUserUnreadNotifications() {
        notificationService.countUserUnreadNotifications(mockUser);

        verify(notificationRepository, times(1)).countByUserAndUnreadIsTrue(any(User.class));
    }

    @Test
    void testSetAsRead() {
        when(notificationRepository.findAllByUser(mockUser)).thenReturn(Optional.of(List.of(mockNotification)));

        notificationService.setAsRead(mockUser);

        assertFalse(mockNotification.unread());

        verify(notificationRepository, times(1)).findAllByUser(mockUser);
    }

    @Test
    void testSendNotification() {
        String message = "New game available!";

        notificationService.sendNotification(message, mockUser);

        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void removeAllUserNotifications() {
        notificationService.removeAllUserNotifications(mockUser);

        verify(notificationRepository, times(1)).removeAllByUser(mockUser);
    }
}