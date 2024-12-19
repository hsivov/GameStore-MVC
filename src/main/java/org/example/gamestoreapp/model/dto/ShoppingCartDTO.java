package org.example.gamestoreapp.model.dto;

import java.math.BigDecimal;
import java.util.List;

public class ShoppingCartDTO {

    private List<GameDTO> games;

    public List<GameDTO> getGames() {
        return games;
    }

    public void setGames(List<GameDTO> games) {
        this.games = games;
    }

    public int getTotalItems() {
        return games != null ? games.size() : 0;
    }

    public BigDecimal getTotalPrice() {
        return games != null ?
                games.stream()
                        .map(GameDTO::getPrice)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                : BigDecimal.ZERO;
    }
}
