package org.example.gamestoreapp.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.example.gamestoreapp.model.dto.ChangePasswordBindingModel;
import org.example.gamestoreapp.model.dto.EditProfileDTO;
import org.example.gamestoreapp.model.dto.ShoppingCartDTO;
import org.example.gamestoreapp.model.view.UserProfileViewModel;
import org.example.gamestoreapp.service.GameService;
import org.example.gamestoreapp.service.ShoppingCartService;
import org.example.gamestoreapp.service.UserService;
import org.example.gamestoreapp.service.session.CartHelperService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.Collections;
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
    public ResponseEntity<?> uploadImage(@RequestParam("profileImage") MultipartFile file, HttpServletRequest request) {
        log.info("Request received from: {}", request.getRemoteAddr());
        log.info("File name: {}", file.getOriginalFilename());
        try {
            String profileImageUrl = userService.uploadProfileImage(file, "profile-images");
            log.info("File uploaded successfully. URL: {}", profileImageUrl);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Collections.singletonMap("profileImageUrl", profileImageUrl));
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error uploading file: " + e.getMessage());
        }
    }

    @GetMapping("/edit-profile")
    public String editProfile(Model model) {

        if (!model.containsAttribute("editProfileDTO")) {
            EditProfileDTO profileDTO = userService.getUserProfile();
            model.addAttribute("editProfileDTO", profileDTO);
        }

        return "edit-profile";
    }

    @PostMapping("/edit-profile")
    public String editProfile(@Valid EditProfileDTO editProfileDTO, BindingResult bindingResult,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("editProfileDTO", editProfileDTO);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.editProfileDTO", bindingResult);


            return "redirect:/user/edit-profile";
        }

        userService.editProfile(editProfileDTO);

        return "redirect:/user/profile";
    }

    @GetMapping("/change-password")
    public String changePassword(@ModelAttribute("message") String message, Model model) {
        if (!model.containsAttribute("changePasswordBindingModel")) {
            model.addAttribute("changePasswordBindingModel", new ChangePasswordBindingModel());
        }

        if (message != null && !message.isEmpty()) {
            model.addAttribute("message", message);
        }

        return "change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(@Valid ChangePasswordBindingModel changePasswordBindingModel,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("changePasswordBindingModel", changePasswordBindingModel);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.changePasswordBindingModel", bindingResult);

            return "redirect:/user/change-password";
        }

        userService.changePassword(changePasswordBindingModel);
        redirectAttributes.addFlashAttribute("message", "Password has been changed successfully.");

        return "redirect:/user/change-password";
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
