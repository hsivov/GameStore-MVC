package org.example.gamestoreapp.model.dto;

import jakarta.validation.constraints.Email;
import org.example.gamestoreapp.validation.annotation.ValidEmail;

public class ForgotPasswordDTO {
    @Email
    @ValidEmail
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
