package org.example.gamestoreapp.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.gamestoreapp.model.dto.ShoppingCartDTO;
import org.example.gamestoreapp.model.view.UserProfileViewModel;
import org.example.gamestoreapp.service.GameService;
import org.example.gamestoreapp.service.ShoppingCartService;
import org.example.gamestoreapp.service.UserService;
import org.example.gamestoreapp.service.session.CartHelperService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final GameService gameService;
    private final ShoppingCartService shoppingCartService;
    private final CartHelperService cartHelperService;

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    public UserController(UserService userService, GameService gameService, ShoppingCartService shoppingCartService, CartHelperService cartHelperService) {
        this.userService = userService;
        this.gameService = gameService;
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

    @PostMapping("/profile/upload-image")
    public ResponseEntity<String> uploadImage(@RequestParam("profileImage") MultipartFile file, HttpServletRequest request) {
        log.info("Request received from: {}", request.getRemoteAddr());
        log.info("File name: {}", file.getOriginalFilename());
        String originalFileName = file.getOriginalFilename();
        return ResponseEntity.ok("");
    }

    @GetMapping("/shopping-cart")
    public String shoppingCart(Model model) {
        ShoppingCartDTO shoppingCartDTO = shoppingCartService.getShoppingCart();

        model.addAttribute("shoppingCart", shoppingCartDTO);
        model.addAttribute("totalPrice", cartHelperService.getTotalPrice());

        return "shopping-cart";
    }

    @PostMapping("/add-to-cart/{gameId}")
    public ResponseEntity<Map<String, Integer>> addToCartWithResponse(@PathVariable("gameId") Long gameId) {
        shoppingCartService.addToCart(gameId);

        // Get the updated cart item count after adding the game
        int totalItems = cartHelperService.getTotalItems();
        Map<String, Integer> response = new HashMap<>();
        response.put("totalItems", totalItems);

        // Return the updated cart count in the response
        return ResponseEntity.ok(response);
    }

    @PostMapping("/add-to-library/{id}")
    public String addToLibrary(@PathVariable("id") Long id) {
        gameService.addToLibrary(id);

        return "redirect:/library";
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
