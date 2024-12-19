package org.example.gamestoreapp.service;

import jakarta.mail.MessagingException;
import org.example.gamestoreapp.model.dto.UserDTO;
import org.example.gamestoreapp.model.view.UserProfileViewModel;
import org.example.gamestoreapp.model.dto.UserRegisterBindingModel;

import java.util.Optional;

public interface UserService {
    boolean register(UserRegisterBindingModel userRegisterBindingModel);

    UserProfileViewModel viewProfile();

    boolean isUniqueEmail(String email);

    boolean isUniqueUsername(String username);

    void resendConfirmationToken(String email) throws MessagingException;

    Optional<UserDTO> getUserById(Long userId);

    void enableUser(String token);
}
