package org.example.gamestoreapp.controller;

import org.example.gamestoreapp.model.dto.ShoppingCartDTO;
import org.example.gamestoreapp.service.ShoppingCartService;
import org.example.gamestoreapp.service.session.CartHelperService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

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
        ShoppingCartDTO shoppingCartDTO = shoppingCartService.getShoppingCart();

        model.addAttribute("shoppingCart", shoppingCartDTO);
        model.addAttribute("totalPrice", cartHelperService.getTotalPrice());

        return "shopping-cart";
    }

    @PostMapping("/shopping-cart/remove/{id}")
    public String shoppingCartRemove(@PathVariable Long id) {
        shoppingCartService.remove(id);
        return "redirect:/shopping-cart";
    }

    @PostMapping("/shopping-cart/remove-all")
    public String shoppingCartRemoveAll() {
        shoppingCartService.removeAll();
        return "redirect:/shopping-cart";
    }
}
