package org.example.gamestoreapp.service;

import jakarta.validation.Valid;
import org.example.gamestoreapp.model.dto.EditProfileDTO;
import org.example.gamestoreapp.model.dto.UserDTO;
import org.example.gamestoreapp.model.view.UserProfileViewModel;

import java.util.Optional;

public interface UserService {

    UserProfileViewModel getProfileView();

    Optional<UserDTO> getUserById(Long userId);

    EditProfileDTO getUserProfile();

    void editProfile(@Valid EditProfileDTO editProfileDTO);
}
