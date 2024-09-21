package org.example.gamestoreapp.controller;

import org.example.gamestoreapp.service.GameService;
import org.example.gamestoreapp.service.ShoppingCartService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class StoreController {

    private final GameService gameService;
    private final ShoppingCartService shoppingCartService;

    public StoreController(GameService gameService, ShoppingCartService shoppingCartService) {
        this.gameService = gameService;
        this.shoppingCartService = shoppingCartService;
    }

    @GetMapping("/store")
    public String store(Model model) {

        model.addAttribute("games", gameService.getAll());

        return "store";
    }

    @PostMapping("/store/add-to-cart/{gameId}")
    public ResponseEntity<Void> addToCart(@PathVariable("gameId") Long gameId) {

        shoppingCartService.addToCart(gameId);

        return ResponseEntity.ok().build();
    }
}
