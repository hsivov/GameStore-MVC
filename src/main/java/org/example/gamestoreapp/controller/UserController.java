package org.example.gamestoreapp.controller;

import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.example.gamestoreapp.exception.AccountConfirmedException;
import org.example.gamestoreapp.exception.TokenExpiredException;
import org.example.gamestoreapp.model.dto.UserLoginBindingModel;
import org.example.gamestoreapp.model.view.UserProfileViewModel;
import org.example.gamestoreapp.model.dto.UserRegisterBindingModel;
import org.example.gamestoreapp.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String login(@RequestParam(value = "confirmed", required = false) String confirmed,
                              @ModelAttribute("userLoginBindingModel") UserLoginBindingModel userLoginBindingModel,
                              Model model) {

        if (confirmed != null) {
            model.addAttribute("message", "Your account is confirmed");
        }

        return "login";
    }

    @GetMapping("/register")
    public String register(Model model) {

        if (!model.containsAttribute("userRegisterBindingModel")) {
            model.addAttribute("userRegisterBindingModel", new UserRegisterBindingModel());
        }

        return "register";
    }

    @PostMapping("/register")
    public ModelAndView register(@Valid UserRegisterBindingModel userRegisterBindingModel,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("userRegisterBindingModel", userRegisterBindingModel);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.userRegisterBindingModel", bindingResult);

            // handle errors
            return new ModelAndView("redirect:/users/register");
        }

        boolean hasSuccessfulRegistration = userService.register(userRegisterBindingModel);

        if (!hasSuccessfulRegistration){
            // If registration failed (user not saved or email not sent), show the registration page with an error
            return new ModelAndView("register", "error", "Registration failed, please try again.");
        }

        // If registration was successful, redirect to login or show a message saying to check email
        redirectAttributes.addFlashAttribute("message", "Registration successful! Please check your email to confirm.");
        // register user
        return new ModelAndView("redirect:/users/login");
    }

    @GetMapping("/confirm")
    public String confirmEmail(@RequestParam("token") String token, RedirectAttributes redirectAttributes) {
        try {
            userService.confirmToken(token);
            return "redirect:/users/login?confirmed";
        } catch (TokenExpiredException e) {

            redirectAttributes.addFlashAttribute("message", e.getMessage());
            redirectAttributes.addFlashAttribute("token", token);

            return "redirect:/users/token-expired";
        } catch (AccountConfirmedException e) {
            redirectAttributes.addFlashAttribute("message", e.getMessage());
            return "redirect:/users/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", e.getMessage());
            return "redirect:/error";
        }
    }

    @PostMapping("/resend-confirmation")
    public String resendConfirmation(@RequestParam("token") String token, RedirectAttributes redirectAttributes) throws MessagingException {
        try {
            userService.resendConfirmationToken(token);
            return "redirect:/users/login?resendSuccess";
        } catch (AccountConfirmedException e) {
            redirectAttributes.addFlashAttribute("message", e.getMessage());
            return "redirect:/users/login";
        }
    }

    @GetMapping("token-expired")
    public String tokenExpiredPage() {

        return "token-expired";
    }

    @GetMapping("/profile")
    public ModelAndView profile() {
        ModelAndView modelAndView = new ModelAndView("profile");

        UserProfileViewModel userProfileViewModel = userService.viewProfile();
        modelAndView.addObject("userProfileViewModel", userProfileViewModel);

        return modelAndView;
    }
}
