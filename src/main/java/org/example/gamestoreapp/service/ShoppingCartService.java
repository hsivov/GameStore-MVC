package org.example.gamestoreapp.service;

import org.example.gamestoreapp.model.dto.GameDTO;

import java.util.Set;

public interface ShoppingCartService {

    void addToCart(Long gameId);

    Set<GameDTO> getAddedGames();
}
