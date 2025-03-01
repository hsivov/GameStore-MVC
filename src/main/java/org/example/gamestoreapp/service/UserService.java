package org.example.gamestoreapp.service;

import jakarta.validation.Valid;
import org.example.gamestoreapp.model.dto.EditProfileDTO;
import org.example.gamestoreapp.model.dto.UserDTO;
import org.example.gamestoreapp.model.view.UserProfileViewModel;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

public interface UserService {

    UserProfileViewModel viewProfile();

    Optional<UserDTO> getUserById(Long userId);

    EditProfileDTO getUserProfile();

    void editProfile(@Valid EditProfileDTO editProfileDTO);
}
