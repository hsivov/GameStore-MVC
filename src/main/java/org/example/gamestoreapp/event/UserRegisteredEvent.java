package org.example.gamestoreapp.event;

import org.example.gamestoreapp.model.entity.User;

public class UserRegisteredEvent {
    private final User user;

    public UserRegisteredEvent(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}
