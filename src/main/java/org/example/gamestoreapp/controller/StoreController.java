package org.example.gamestoreapp.controller;

import org.example.gamestoreapp.service.GameService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StoreController {

    private final GameService gameService;

    public StoreController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping("/store")
    public String store(Model model) {

        model.addAttribute("games", gameService.getAll());

        return "store";
    }
}
