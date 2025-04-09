package org.example.gamestoreapp.controller;

import org.example.gamestoreapp.service.CheckoutService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
    public String payment(@RequestParam("paymentMethod") String paymentMethod) {
       checkoutService.payment(paymentMethod);

       return "redirect:/library";
    }
}
