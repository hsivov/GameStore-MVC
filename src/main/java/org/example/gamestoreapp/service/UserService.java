package org.example.gamestoreapp.service;

import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.example.gamestoreapp.model.dto.EditProfileDTO;
import org.example.gamestoreapp.model.dto.UserDTO;
import org.example.gamestoreapp.model.view.UserProfileViewModel;
import org.example.gamestoreapp.model.dto.UserRegisterBindingModel;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

public interface UserService {
    boolean register(UserRegisterBindingModel userRegisterBindingModel);

    UserProfileViewModel viewProfile();

    String uploadProfileImage(MultipartFile file, String containerName) throws IOException;

    boolean isUniqueEmail(String email);

    boolean isUniqueUsername(String username);

    void resendConfirmationToken(String email) throws MessagingException;

    Optional<UserDTO> getUserById(Long userId);

    void enableUser(String token);

    EditProfileDTO getUserProfile();

    void editProfile(@Valid EditProfileDTO editProfileDTO);
}
