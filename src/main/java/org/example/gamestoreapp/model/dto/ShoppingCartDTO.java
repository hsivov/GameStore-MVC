package org.example.gamestoreapp.model.dto;

import java.math.BigDecimal;
import java.util.List;

public class ShoppingCartDTO {

    private List<GameDTO> games;

    private int totalItems;

    private BigDecimal totalPrice;

    public List<GameDTO> getGames() {
        return games;
    }

    public void setGames(List<GameDTO> games) {
        this.games = games;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }
}
