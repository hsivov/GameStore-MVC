package org.example.gamestoreapp.controller;

import org.example.gamestoreapp.model.dto.GameDTO;
import org.example.gamestoreapp.service.ShoppingCartService;
import org.example.gamestoreapp.service.session.CartHelperService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Set;

@Controller
public class ShoppingCartController {
    private final ShoppingCartService shoppingCartService;
    private final CartHelperService cartHelperService;

    public ShoppingCartController(ShoppingCartService shoppingCartService, CartHelperService cartHelperService) {
        this.shoppingCartService = shoppingCartService;
        this.cartHelperService = cartHelperService;
    }

    @GetMapping("/shopping-cart")
    public String shoppingCart(Model model) {
        Set<GameDTO> addedGames = shoppingCartService.getAddedGames();
        model.addAttribute("games", addedGames);
        model.addAttribute("totalPrice", cartHelperService.getTotalPrice());

        return "/shopping-cart";
    }
}
