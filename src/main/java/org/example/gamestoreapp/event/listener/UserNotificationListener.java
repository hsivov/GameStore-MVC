package org.example.gamestoreapp.event.listener;

import org.example.gamestoreapp.event.*;
import org.example.gamestoreapp.model.dto.OrderResponseDTO;
import org.example.gamestoreapp.model.entity.User;
import org.example.gamestoreapp.service.NotificationService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class UserNotificationListener {

    private final NotificationService notificationService;

    public UserNotificationListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @EventListener
    @Async
    public void onUserRegistered(UserRegisteredEvent event) {
        User user = event.getUser();
        notificationService.sendNotification("Your registration is successful.", user);
    }

    @EventListener
    @Async
    public void onPasswordChanged(PasswordChangedEvent event) {
        User user = event.getUser();
        notificationService.sendNotification("Your password has been changed.", user);
    }

    @EventListener
    @Async
    public void onPasswordReset(PasswordResetEvent event) {
        User user = event.getUser();
        notificationService.sendNotification("Your password has been successfully reset.", user);
    }

    @EventListener
    @Async
    public void onUserEnabled(UserEnabledEvent event) {
        User user = event.getUser();
        notificationService.sendNotification("Your account has been confirmed.", user);
    }

    @EventListener
    @Async
    public void onOrderApproved(OrderApprovedEvent event) {
        OrderResponseDTO order = event.getOrder();
        User user = event.getUser();
        notificationService.sendNotification("Thank you for your recent purchase! Your order #" + order.getId() + " has been successfully processed. " +
                "You can find purchased games in your library.", user);
    }

    @EventListener
    @Async
    public void onOrderRejected(OrderRejectedEvent event) {
        OrderResponseDTO order = event.getOrder();
        User user = event.getUser();
        notificationService.sendNotification("Your recent payment attempt for order #" + order.getId() +
                " on " + order.getOrderDate() + " was unsuccessful. " +
                "This could be due to various reasons, such as insufficient funds, incorrect details, or a bank authorization issue.", user);
    }
}
