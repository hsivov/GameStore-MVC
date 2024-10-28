package org.example.gamestoreapp.controller;

import org.example.gamestoreapp.model.dto.ShoppingCartDTO;
import org.example.gamestoreapp.model.view.UserProfileViewModel;
import org.example.gamestoreapp.service.ShoppingCartService;
import org.example.gamestoreapp.service.UserService;
import org.example.gamestoreapp.service.session.CartHelperService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final ShoppingCartService shoppingCartService;
    private final CartHelperService cartHelperService;

    public UserController(UserService userService, ShoppingCartService shoppingCartService, CartHelperService cartHelperService) {
        this.userService = userService;
        this.shoppingCartService = shoppingCartService;
        this.cartHelperService = cartHelperService;
    }

    @GetMapping("/profile")
    public ModelAndView profile() {
        ModelAndView modelAndView = new ModelAndView("profile");

        UserProfileViewModel userProfileViewModel = userService.viewProfile();
        modelAndView.addObject("userProfileViewModel", userProfileViewModel);

        return modelAndView;
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
        return "redirect:/user/shopping-cart";
    }

    @PostMapping("/shopping-cart/remove-all")
    public String shoppingCartRemoveAll() {
        shoppingCartService.removeAll();
        return "redirect:/user/shopping-cart";
    }
}
