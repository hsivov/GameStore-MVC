package org.example.gamestoreapp.service;

import org.example.gamestoreapp.model.dto.AddGameBindingModel;
import org.example.gamestoreapp.model.dto.UpdateGameBindingModel;
import org.example.gamestoreapp.model.dto.GameDTO;
import org.example.gamestoreapp.model.dto.UserDTO;

import java.util.List;

public interface AdminService {
    void addGame(AddGameBindingModel addGameBindingModel);

    List<UserDTO> getAllUsers();

    void promote(long id);

    void deleteUser(long id);

    List<GameDTO> getAllGames();

    void deleteGame(Long id);

    UpdateGameBindingModel getById(Long id);

    void editGame(UpdateGameBindingModel updateGameBindingModel, Long id);
}
