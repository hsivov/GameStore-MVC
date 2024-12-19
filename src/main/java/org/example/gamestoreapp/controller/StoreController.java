package org.example.gamestoreapp.controller;

import jakarta.validation.Valid;
import org.example.gamestoreapp.exception.GameNotFoundException;
import org.example.gamestoreapp.model.dto.CommentDTO;
import org.example.gamestoreapp.model.dto.GameDTO;
import org.example.gamestoreapp.model.dto.PostCommentDTO;
import org.example.gamestoreapp.service.CommentService;
import org.example.gamestoreapp.service.GameService;
import org.example.gamestoreapp.service.LibraryService;
import org.example.gamestoreapp.service.ShoppingCartService;
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
    private final LibraryService libraryService;
    private final CommentService commentService;

    public StoreController(GameService gameService, ShoppingCartService shoppingCartService, LibraryService libraryService, CommentService commentService) {
        this.gameService = gameService;
        this.shoppingCartService = shoppingCartService;
        this.libraryService = libraryService;
        this.commentService = commentService;
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

        Map<Long, Boolean> gamesInShoppingCart = new HashMap<>();

        games.forEach(gameDTO -> {
            boolean isInCart = shoppingCartService.isGameInCart(gameDTO.getId());
            gamesInShoppingCart.put(gameDTO.getId(), isInCart);
        });

        model.addAttribute("gamesInShoppingCart", gamesInShoppingCart);
        return "store";
    }

    @GetMapping("/store/game-details/{id}")
    public String gameDetails(@PathVariable Long id, Model model) {
        GameDTO gameById = gameService.getGameById(id)
                .orElseThrow(() -> new GameNotFoundException("Game with ID " + id + " not found"));

        boolean isInLibrary = libraryService.isGameInLibrary(id);
        boolean isInCart = shoppingCartService.isGameInCart(id);

        List<CommentDTO> comments = commentService.getCommentsByGame(id);

        model.addAttribute("game", gameById);
        model.addAttribute("isInLibrary", isInLibrary);
        model.addAttribute("isInCart", isInCart);
        model.addAttribute("comments", comments);

        return "game-details";
    }

    @PostMapping("/game-details/add-to-cart/{gameId}")
    public String addToCartFromDetails(@PathVariable("gameId") Long id) {
        shoppingCartService.addToCart(id);

        return "redirect:/store/game-details/{gameId}";
    }

    @PostMapping("/game-details/post-comment/{gameId}")
    public String postComment(@PathVariable("gameId") Long gameId,
                              @Valid PostCommentDTO postCommentDTO) {
        commentService.postComment(postCommentDTO, gameId);

        return "redirect:/store/game-details/{gameId}";
    }
}
