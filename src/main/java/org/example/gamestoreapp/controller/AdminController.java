package org.example.gamestoreapp.controller;

import jakarta.validation.Valid;
import org.example.gamestoreapp.model.dto.AddGameBindingModel;
import org.example.gamestoreapp.model.enums.GenreName;
import org.example.gamestoreapp.service.AdminService;
import org.example.gamestoreapp.util.GenreConverter;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/add-game")
    public ModelAndView addGame(@ModelAttribute("addGameBindingModel")AddGameBindingModel addGameBindingModel) {
        ModelAndView modelAndView = new ModelAndView("add-game");
        Map<GenreName, String> genreDescriptions = GenreConverter.getAllGenreDescriptions();
        modelAndView.addObject("genres", genreDescriptions);

        return modelAndView;
    }

    @PostMapping("/add-game")
    public ModelAndView addGame(@ModelAttribute("addGameBindingModel") @Valid AddGameBindingModel addGameBindingModel,
                                BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("addGameBindingModel", addGameBindingModel);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.addGameBindingModel", bindingResult);

            // handle errors
            return new ModelAndView("add-game");
        }

        adminService.addGame(addGameBindingModel);

        return new ModelAndView("redirect:/");
    }
}
