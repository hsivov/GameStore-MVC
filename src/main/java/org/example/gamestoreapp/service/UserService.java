package org.example.gamestoreapp.service;

import jakarta.mail.MessagingException;
import org.example.gamestoreapp.model.view.UserProfileViewModel;
import org.example.gamestoreapp.model.dto.UserRegisterBindingModel;

public interface UserService {
    boolean register(UserRegisterBindingModel userRegisterBindingModel);

    UserProfileViewModel viewProfile();

    boolean isUniqueEmail(String email);

    boolean isUniqueUsername(String username);

    void confirmToken(String token);

    void resendConfirmationToken(String email) throws MessagingException;
}
