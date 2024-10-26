package org.example.gamestoreapp.controller;

import jakarta.mail.MessagingException;
import org.example.gamestoreapp.service.CheckoutService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Controller
public class CheckoutController {
    private final CheckoutService checkoutService;

    public CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @GetMapping("/checkout")
    public String checkout() {
        return "checkout";
    }

    @PostMapping("/payment")
    public String payment() throws MessagingException, NoSuchAlgorithmException, InvalidKeyException {
       checkoutService.payment();

       return "redirect:/library";
    }
}
