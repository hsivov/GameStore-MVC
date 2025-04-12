package org.example.gamestoreapp.event;

import org.example.gamestoreapp.model.entity.User;

public class UserEnabledEvent {
    private final User user;

    public UserEnabledEvent(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}
