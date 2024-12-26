package org.example.gamestoreapp.service;

import org.example.gamestoreapp.model.dto.*;

import java.io.IOException;
import java.util.List;

public interface AdminService {
    void addGame(AddGameBindingModel addGameBindingModel) throws IOException;

    List<UserDTO> getAllUsers();

    void promote(long id);

    void toggleUserState(long id);

    void demote(long id);

    List<GameDTO> getAllGames();

    void deleteGame(Long id);

    UpdateGameBindingModel getById(Long id);

    void editGame(UpdateGameBindingModel updateGameBindingModel, Long id) throws IOException;

    List<GenreDTO> getAllGenres();
}
