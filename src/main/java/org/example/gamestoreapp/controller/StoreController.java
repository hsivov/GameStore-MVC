package org.example.gamestoreapp.controller;

import org.example.gamestoreapp.service.GameService;
import org.example.gamestoreapp.service.ShoppingCartService;
import org.example.gamestoreapp.service.session.CartHelperService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.HashMap;
import java.util.Map;

@Controller
public class StoreController {

    private final GameService gameService;
    private final ShoppingCartService shoppingCartService;
    private final CartHelperService cartHelperService;

    public StoreController(GameService gameService, ShoppingCartService shoppingCartService, CartHelperService cartHelperService) {
        this.gameService = gameService;
        this.shoppingCartService = shoppingCartService;
        this.cartHelperService = cartHelperService;
    }

    @GetMapping("/store")
    public String store(Model model) {

        model.addAttribute("games", gameService.getAll());

        return "store";
    }

    @PostMapping("/store/add-to-cart/{gameId}")
    public ResponseEntity<Map<String, Integer>> addToCart(@PathVariable("gameId") Long gameId) {
        shoppingCartService.addToCart(gameId);

        // Get the updated cart item count after adding the game
        int totalItems = cartHelperService.getTotalItems();
        Map<String, Integer> response = new HashMap<>();
        response.put("totalItems", totalItems);

        // Return the updated cart count in the response
        return ResponseEntity.ok(response);
    }

    @PostMapping("/store/add-to-library/{id}")
    public String addToLibrary(@PathVariable("id") Long id) {
        gameService.addToLibrary(id);

        return "redirect:/library";
    }
}
