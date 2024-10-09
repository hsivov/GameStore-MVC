package org.example.gamestoreapp.controller;

import org.example.gamestoreapp.service.CheckoutService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

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
    public String payment() {
       checkoutService.payment();

       return "redirect:/library";
    }
}
