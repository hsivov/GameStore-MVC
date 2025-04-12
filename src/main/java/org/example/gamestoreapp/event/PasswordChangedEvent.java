package org.example.gamestoreapp.event;

import org.example.gamestoreapp.model.entity.User;

public class PasswordChangedEvent {
    private final User user;

    public PasswordChangedEvent(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}
