package org.example.gamestoreapp.service;

import org.example.gamestoreapp.model.dto.GameDTO;

import java.util.List;
import java.util.Set;

public interface GameService {
    List<GameDTO> getAll();

    Set<GameDTO> getOwnedGames();

    void addToLibrary(Long id);
}
