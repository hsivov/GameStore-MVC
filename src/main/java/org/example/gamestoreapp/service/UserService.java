package org.example.gamestoreapp.service;

import org.example.gamestoreapp.model.view.UserProfileViewModel;
import org.example.gamestoreapp.model.dto.UserRegisterBindingModel;

public interface UserService {
    boolean register(UserRegisterBindingModel userRegisterBindingModel);

    UserProfileViewModel viewProfile();

    boolean isUniqueEmail(String email);

    boolean isUniqueUsername(String username);
}
