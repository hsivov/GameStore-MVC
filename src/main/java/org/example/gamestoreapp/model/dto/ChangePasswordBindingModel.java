package org.example.gamestoreapp.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.example.gamestoreapp.validation.annotation.CorrectPassword;
import org.example.gamestoreapp.validation.annotation.PasswordMatches;

@PasswordMatches
public class ChangePasswordBindingModel {

    @CorrectPassword
    private String currentPassword;
    @NotBlank(message = "Field cannot be blank!")
    @Size(min = 4, max = 16, message = "Password length must be between 4 and 20 characters!")
    private String newPassword;
    private String confirmPassword;

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
