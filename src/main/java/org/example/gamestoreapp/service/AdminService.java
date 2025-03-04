package org.example.gamestoreapp.service;

import jakarta.validation.Valid;
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

    UpdateGameBindingModel getGameById(Long id);

    void editGame(UpdateGameBindingModel updateGameBindingModel) throws IOException;

    List<GenreDTO> getAllGenres();

    void addGenre(@Valid AddGenreBindingModel addGenreBindingModel);

    UpdateGenreBindingModel getGenreById(long id);

    void editGenre(@Valid UpdateGenreBindingModel updateGenreBindingModel);

    void deleteGenre(Long id);
}
