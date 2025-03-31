package org.example.gamestoreapp.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.gamestoreapp.model.dto.GameDTO;
import org.example.gamestoreapp.service.GameService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    private final GameService gameService;

    public HomeController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping("/")
    public String home(HttpServletRequest request, Model model) {
        model.addAttribute("currentUrl", request.getRequestURI());
        return "index";
    }

    @GetMapping("/library")
    public String library(Model model) {
        List<GameDTO> ownedGames = gameService.getOwnedGames();
        model.addAttribute("ownedGames", ownedGames);

        return "library";
    }

    @GetMapping("/contacts")
    public String contacts() {
        return "contacts";
    }
}
