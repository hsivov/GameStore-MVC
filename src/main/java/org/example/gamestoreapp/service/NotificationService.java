package org.example.gamestoreapp.service;

import org.example.gamestoreapp.model.dto.NotificationDTO;
import org.example.gamestoreapp.model.entity.User;

import java.util.List;

public interface NotificationService {
    List<NotificationDTO> getUserNotifications(User user);

    long countUserUnreadNotifications();

    void setAsRead(User user);

    void sendNotification(String message, User receiver);

    void removeAllUserNotifications(User currentUser);
}
