package org.example.gamestoreapp.service;

import org.example.gamestoreapp.model.dto.GameDTO;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface GameService {
    List<GameDTO> getAll();

    List<GameDTO> getOwnedGames();

    void addToLibrary(Long id);

    Optional<GameDTO> getGameById(Long id);

    List<GameDTO> getGamesByIds(Set<Long> gameIds);

    void updatePricesAt0010();
}
