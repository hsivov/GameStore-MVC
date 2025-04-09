package org.example.gamestoreapp.service;

import jakarta.validation.Valid;
import org.example.gamestoreapp.model.dto.ChangePasswordBindingModel;
import org.example.gamestoreapp.model.dto.ResetPasswordDTO;
import org.example.gamestoreapp.model.dto.UserRegisterBindingModel;

public interface AuthService {
    boolean register(UserRegisterBindingModel userRegisterBindingModel);

    void enableUser(String token);

    void resendConfirmationToken(String token);

    boolean isUniqueEmail(String email);

    boolean isUniqueUsername(String username);

    boolean isCorrectPassword(String password);

    void changePassword(@Valid ChangePasswordBindingModel changePasswordBindingModel);

    boolean isValidEmail(String email);

    void passwordResetRequest(String email);

    void resetPassword(@Valid ResetPasswordDTO resetPasswordDTO, String resetToken);
}
