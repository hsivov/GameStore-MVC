package org.example.gamestoreapp.controller;

import org.example.gamestoreapp.exception.GameNotFoundException;
import org.example.gamestoreapp.model.dto.GameDTO;
import org.example.gamestoreapp.service.GameService;
import org.example.gamestoreapp.service.LibraryService;
import org.example.gamestoreapp.service.ShoppingCartService;
import org.example.gamestoreapp.service.session.CartHelperService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class StoreController {

    private final GameService gameService;
    private final ShoppingCartService shoppingCartService;
    private final CartHelperService cartHelperService;
    private final LibraryService libraryService;

    public StoreController(GameService gameService, ShoppingCartService shoppingCartService, CartHelperService cartHelperService, LibraryService libraryService) {
        this.gameService = gameService;
        this.shoppingCartService = shoppingCartService;
        this.cartHelperService = cartHelperService;
        this.libraryService = libraryService;
    }

    @GetMapping("/store")
    public String store(Model model) {

        List<GameDTO> games = gameService.getAll();
        model.addAttribute("games", games);

        Map<Long, Boolean> gamesInLibrary = new HashMap<>();

        for (GameDTO gameDTO : games) {
            boolean inLibrary = libraryService.isGameInLibrary(gameDTO.getId());
            gamesInLibrary.put(gameDTO.getId(), inLibrary);
        }

        model.addAttribute("gamesInLibrary", gamesInLibrary);

        return "store";
    }

    @PostMapping("/game-details/add-to-cart/{gameId}")
    public String addToCartFromDetails(@PathVariable("gameId") Long id) {
        shoppingCartService.addToCart(id);

        return "redirect:/shopping-cart";
    }

    @PostMapping("/store/add-to-cart/{gameId}")
    public ResponseEntity<Map<String, Integer>> addToCartWithResponse(@PathVariable("gameId") Long gameId) {
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

    @GetMapping("/game-details/{id}")
    public String gameDetails(@PathVariable Long id, Model model) {
        GameDTO gameById = gameService.getGameById(id)
                .orElseThrow(() -> new GameNotFoundException("Game with ID " + id + " not found"));

        model.addAttribute("game", gameById);

        return "game-details";
    }
}
