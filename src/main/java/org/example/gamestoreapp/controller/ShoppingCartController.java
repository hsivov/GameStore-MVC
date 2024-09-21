package org.example.gamestoreapp.controller;

import org.example.gamestoreapp.model.dto.GameDTO;
import org.example.gamestoreapp.service.ShoppingCartService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Set;

@Controller
public class ShoppingCartController {
    private final ShoppingCartService shoppingCartService;

    public ShoppingCartController(ShoppingCartService shoppingCartService) {
        this.shoppingCartService = shoppingCartService;
    }

    @GetMapping("/shopping-cart")
    public String shoppingCart(Model model) {
        Set<GameDTO> addedGames = shoppingCartService.getAddedGames();
        model.addAttribute("games", addedGames);

        return "/shopping-cart";
    }
}
