package org.example.gamestoreapp.event;

import org.example.gamestoreapp.model.entity.User;

public class PasswordResetRequestEvent {
    private final User user;

    public PasswordResetRequestEvent(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}
