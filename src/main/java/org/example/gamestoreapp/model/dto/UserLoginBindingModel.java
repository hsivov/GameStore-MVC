package org.example.gamestoreapp.model.dto;

import jakarta.validation.constraints.Size;

public class UserLoginBindingModel {
    @Size(min = 3, max = 30, message = "Username length must be between 3 and 30 characters!")
    private String username;
    @Size(min = 4, max = 20, message = "Password length must be between 4 and 20 characters!")
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
