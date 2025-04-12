package org.example.gamestoreapp.event;

import org.example.gamestoreapp.model.entity.User;

public class PasswordResetEvent {
    private final User user;

    public PasswordResetEvent(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}
