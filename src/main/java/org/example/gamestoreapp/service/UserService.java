package org.example.gamestoreapp.service;

import org.example.gamestoreapp.model.dto.UserRegisterBindingModel;

public interface UserService {
    boolean register(UserRegisterBindingModel userRegisterBindingModel);
}
