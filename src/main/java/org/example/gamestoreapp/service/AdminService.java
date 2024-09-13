package org.example.gamestoreapp.service;

import org.example.gamestoreapp.model.dto.AddGameBindingModel;
import org.example.gamestoreapp.model.dto.GameDTO;
import org.example.gamestoreapp.model.dto.UserDTO;

import java.util.List;

public interface AdminService {
    void addGame(AddGameBindingModel addGameBindingModel);

    List<UserDTO> getAllUsers();

    void promote(long id);

    void delete(long id);

    List<GameDTO> getAllGames();
}
