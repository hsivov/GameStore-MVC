package org.example.gamestoreapp.model.dto;

import jakarta.validation.constraints.Size;
import org.example.gamestoreapp.validation.annotation.PasswordMatches;

@PasswordMatches
public class ResetPasswordDTO {
    @Size(min = 4, max = 16)
    private String newPassword;
    private String confirmPassword;

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
