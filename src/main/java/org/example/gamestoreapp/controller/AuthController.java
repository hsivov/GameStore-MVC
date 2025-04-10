package org.example.gamestoreapp.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.example.gamestoreapp.exception.IllegalTokenException;
import org.example.gamestoreapp.exception.TokenExpiredException;
import org.example.gamestoreapp.exception.UsedTokenException;
import org.example.gamestoreapp.model.dto.ForgotPasswordDTO;
import org.example.gamestoreapp.model.dto.ResetPasswordDTO;
import org.example.gamestoreapp.model.dto.UserLoginBindingModel;
import org.example.gamestoreapp.model.dto.UserRegisterBindingModel;
import org.example.gamestoreapp.service.AuthService;
import org.example.gamestoreapp.service.TokenService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
public class AuthController {
    private final TokenService tokenService;
    private final AuthService authService;

    public AuthController(TokenService tokenService, AuthService authService) {
        this.tokenService = tokenService;
        this.authService = authService;
    }

    @GetMapping("/login")
    public String login(@RequestParam(value = "confirmed", required = false) String confirmed,
                        @ModelAttribute("userLoginBindingModel") UserLoginBindingModel userLoginBindingModel,
                        Model model) {

        if (confirmed != null) {
            model.addAttribute("message", "Your account has been confirmed");
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
                                 RedirectAttributes redirectAttributes,
                                 HttpServletResponse response) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("userRegisterBindingModel", userRegisterBindingModel);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.userRegisterBindingModel", bindingResult);

            // handle errors
            return new ModelAndView("redirect:/auth/register");
        }

        boolean hasSuccessfulRegistration = authService.register(userRegisterBindingModel);

        if (!hasSuccessfulRegistration){
            // If registration failed (user not saved or email not sent), show the registration page with an error
            response.setStatus(500);
            return new ModelAndView("register", "error", "Registration failed, please try again.");
        }

        // If registration was successful, redirect to login or show a message saying to check email
        redirectAttributes.addFlashAttribute("message", "Registration successful! Please check your email to confirm.");
        // register user
        return new ModelAndView("redirect:/auth/login");
    }

    @GetMapping("/confirm")
    public String confirmEmail(@RequestParam("token") String token, RedirectAttributes redirectAttributes) {
        try {
            tokenService.verifyToken(token);
            authService.enableUser(token);
            return "redirect:/auth/login?confirmed";
        } catch (TokenExpiredException e) {

            redirectAttributes.addFlashAttribute("message", e.getMessage());
            redirectAttributes.addFlashAttribute("token", token);

            return "redirect:/auth/token-expired";
        } catch (UsedTokenException e) {
            redirectAttributes.addFlashAttribute("message", e.getMessage());
            return "redirect:/auth/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", e.getMessage());
            return "redirect:/error";
        }
    }

    @PostMapping("/resend-confirmation")
    public String resendConfirmation(@RequestParam("token") String token, RedirectAttributes redirectAttributes) {
        try {
            authService.resendConfirmationToken(token);
            return "redirect:/auth/login?resendSuccess";
        } catch (UsedTokenException e) {
            redirectAttributes.addFlashAttribute("message", e.getMessage());
            return "redirect:/auth/login";
        }
    }

    @GetMapping("token-expired")
    public String tokenExpiredPage() {

        return "token-expired";
    }

    @GetMapping("/forgotten-password")
    public String forgotPassword(@ModelAttribute("message") String message, Model model) {
        if (!model.containsAttribute("forgotPasswordDTO")) {
            model.addAttribute("forgotPasswordDTO", new ForgotPasswordDTO());
        }

        model.addAttribute("message", message);

        return "forgotten-password";
    }

    @PostMapping("/forgotten-password")
    public String forgotPassword(@Valid ForgotPasswordDTO forgotPasswordDTO, BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("forgotPasswordDTO", forgotPasswordDTO);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.forgotPasswordDTO", bindingResult);

            return "redirect:/auth/forgotten-password";
        }

        authService.passwordResetRequest(forgotPasswordDTO.getEmail());
        redirectAttributes.addFlashAttribute("message", "An email has been sent to you with instructions how to reset your password.");

        return "redirect:/auth/forgotten-password";
    }

    @GetMapping("/confirm/reset-password")
    public String confirmResetPassword(@RequestParam("token") String token) {
        try {
            tokenService.verifyToken(token);

            return "redirect:/auth/reset-password?token=" + token;

        } catch (IllegalTokenException | UsedTokenException | TokenExpiredException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "The token is invalid or has expired.", e);
        }
    }

    @GetMapping("/reset-password")
    public String resetPassword(@ModelAttribute("message") String message,
                                @ModelAttribute("errorMessage") String errorMessage, Model model,
                                @RequestParam(value = "token") String token) {
        if (!model.containsAttribute("resetPasswordDTO")) {
            model.addAttribute("resetPasswordDTO", new ResetPasswordDTO());
        }

        model.addAttribute("message", message);
        model.addAttribute("errorMessage", errorMessage);
        model.addAttribute("token", token);

        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@Valid ResetPasswordDTO resetPasswordDTO, BindingResult bindingResult,
                                @RequestParam("token") String token,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("resetPasswordDTO", resetPasswordDTO);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.resetPasswordDTO", bindingResult);

            return "redirect:/auth/reset-password?token=" + token;
        }

        try {
            tokenService.verifyToken(token);
            authService.resetPassword(resetPasswordDTO, token);
            redirectAttributes.addFlashAttribute("message", "Your password has been reset. You can now log in with your new password.");

            return "redirect:/auth/login";
        } catch (IllegalTokenException | UsedTokenException | TokenExpiredException e) {
            return "redirect:/auth/reset-password?token=" + token;
        }
    }
}
