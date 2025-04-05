package org.example.gamestoreapp.service.impl;

import jakarta.transaction.Transactional;
import org.example.gamestoreapp.model.dto.NotificationDTO;
import org.example.gamestoreapp.model.entity.Notification;
import org.example.gamestoreapp.model.entity.User;
import org.example.gamestoreapp.repository.NotificationRepository;
import org.example.gamestoreapp.service.NotificationService;
import org.example.gamestoreapp.service.session.UserHelperService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserHelperService userHelperService;

    public NotificationServiceImpl(NotificationRepository notificationRepository, UserHelperService userHelperService) {
        this.notificationRepository = notificationRepository;
        this.userHelperService = userHelperService;
    }

    @Override
    public List<NotificationDTO> getUserNotifications(User user) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

        return notificationRepository.findAllByUser(user)
                .orElse(Collections.emptyList())
                .stream()
                .map(notification -> {
                    NotificationDTO dto = new NotificationDTO();
                    dto.setId(notification.getId());
                    dto.setMessage(notification.getMessage());
                    dto.setUnread(notification.unread());
                    dto.setCreatedAt(formatter.format(notification.getCreatedAt()));

                    return dto;
                })
                .toList();
    }

    @Override
    public long countUserUnreadNotifications() {
        User currentUser = userHelperService.getUser();
        return notificationRepository.countByUserAndUnreadIsTrue(currentUser);
    }

    @Override
    public void setAsRead(User user) {
        List<Notification> notifications = notificationRepository.findAllByUser(user)
                .orElse(Collections.emptyList())
                .stream()
                .peek(notification -> notification.setUnread(false))
                .toList();

        notificationRepository.saveAll(notifications);
    }

    @Override
    public void sendNotification(String message, User receiver) {
        Notification notification = new Notification();
        notification.setMessage(message);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setUser(receiver);

        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void removeAllUserNotifications(User currentUser) {
        notificationRepository.removeAllByUser(currentUser);
    }
}
