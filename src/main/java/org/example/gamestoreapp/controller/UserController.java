package org.example.gamestoreapp.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.example.gamestoreapp.model.dto.*;
import org.example.gamestoreapp.model.entity.User;
import org.example.gamestoreapp.model.view.UserProfileViewModel;
import org.example.gamestoreapp.service.*;
import org.example.gamestoreapp.service.session.UserHelperService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final GameService gameService;
    private final ShoppingCartService shoppingCartService;
    private final OrderService orderService;
    private final AuthService authService;
    private final UserHelperService userHelperService;
    private final AzureBlobStorageService azureBlobStorageService;
    private final NotificationService notificationService;

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    public UserController(UserService userService, GameService gameService, ShoppingCartService shoppingCartService, OrderService orderService, AuthService authService, UserHelperService userHelperService, AzureBlobStorageService azureBlobStorageService, NotificationService notificationService) {
        this.userService = userService;
        this.gameService = gameService;
        this.shoppingCartService = shoppingCartService;
        this.orderService = orderService;
        this.authService = authService;
        this.userHelperService = userHelperService;
        this.azureBlobStorageService = azureBlobStorageService;
        this.notificationService = notificationService;
    }

    @GetMapping("/profile")
    public ModelAndView profile() {
        ModelAndView modelAndView = new ModelAndView("profile");

        UserProfileViewModel userProfileViewModel = userService.getProfileView();
        modelAndView.addObject("userProfileViewModel", userProfileViewModel);

        return modelAndView;
    }

    @PostMapping("/profile/upload-image")
    public ResponseEntity<?> uploadImage(@RequestParam("profileImage") MultipartFile file, HttpServletRequest request) {
        log.info("Request received from: {}", request.getRemoteAddr());
        log.info("File name: {}", file.getOriginalFilename());
        try {
            String profileImageUrl = azureBlobStorageService.uploadToAzureBlobStorage(file, "profile-images");
            log.info("File uploaded successfully. URL: {}", profileImageUrl);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Collections.singletonMap("profileImageUrl", profileImageUrl));
        } catch (IOException e) {
            return ResponseEntity.status(500)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("Error uploading file: " + e.getMessage());
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

        return "change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(@Valid ChangePasswordBindingModel changePasswordBindingModel,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("changePasswordBindingModel", changePasswordBindingModel);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.changePasswordBindingModel", bindingResult);
            redirectAttributes.addFlashAttribute("message", "Validation error!");

            return "redirect:/user/change-password";
        }

        authService.changePassword(changePasswordBindingModel);
        redirectAttributes.addFlashAttribute("message", "Password has been changed successfully.");

        return "redirect:/user/change-password";
    }

    @GetMapping("/shopping-cart")
    public String shoppingCart(Model model) {
        ShoppingCartDTO shoppingCartDTO = shoppingCartService.getShoppingCart();

        model.addAttribute("shoppingCart", shoppingCartDTO);

        return "shopping-cart";
    }

    @PostMapping("/add-to-cart/{gameId}")
    public ResponseEntity<Map<String, Integer>> addToCartWithResponse(@PathVariable("gameId") Long gameId) {
        shoppingCartService.addToCart(gameId);

        // Get the updated cart item count after adding the game
        int totalItems = shoppingCartService.getShoppingCart().getTotalItems();
        Map<String, Integer> response = new HashMap<>();
        response.put("totalItems", totalItems);

        // Return the updated cart count in the response
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }

    @PostMapping("/add-to-library/{id}")
    public String addToLibrary(@PathVariable("id") Long id) {
        gameService.addToLibrary(id);

        return "redirect:/library";
    }

    @PostMapping("/shopping-cart/remove/{id}")
    public String shoppingCartRemove(@PathVariable Long id) {
        shoppingCartService.removeItem(id);
        return "redirect:/user/shopping-cart";
    }

    @PostMapping("/shopping-cart/remove-all")
    public String shoppingCartRemoveAll() {
        shoppingCartService.removeAll();
        return "redirect:/user/shopping-cart";
    }

    @GetMapping("/orders")
    public String orders(Model model) {
        long userId = userHelperService.getUser().getId();
        List<OrderResponseDTO> orders = orderService.getOrdersByUser(userId);

        model.addAttribute("orders", orders);
        return "user-orders";
    }

    @GetMapping("/order/{orderId}")
    public String getOrderDetails(@PathVariable("orderId") long id, Model model) {
        OrderResponseDTO order = orderService.getOrderById(id);
        model.addAttribute("order", order);
        return "order-details";
    }

    @GetMapping("/notifications")
    public String getNotifications(Model model) {
        User currentUser = userHelperService.getUser();
        List<NotificationDTO> notifications = notificationService.getUserNotifications(currentUser);

        model.addAttribute("notifications", notifications);

        notificationService.setAsRead(currentUser);

        return "notifications";
    }

    @PostMapping("/notifications/remove-all")
    public String removeAllNotifications() {
        User currentUser = userHelperService.getUser();

        notificationService.removeAllUserNotifications(currentUser);

        return "redirect:/user/notifications";
    }
}
