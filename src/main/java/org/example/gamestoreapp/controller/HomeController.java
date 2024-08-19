package org.example.gamestoreapp.controller;

import org.example.gamestoreapp.service.GameService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    private final GameService gameService;

    public HomeController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping("/")
    public String index(Model model) {

        model.addAttribute("games", gameService.getAll());

        return "index";
    }
}
