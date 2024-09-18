package org.example.gamestoreapp.controller;

import jakarta.validation.Valid;
import org.example.gamestoreapp.model.dto.AddGameBindingModel;
import org.example.gamestoreapp.model.dto.UpdateGameBindingModel;
import org.example.gamestoreapp.model.enums.GenreName;
import org.example.gamestoreapp.service.AdminService;
import org.example.gamestoreapp.util.GenreConverter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
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

    @GetMapping("/users")
    public ModelAndView users() {
        ModelAndView view = new ModelAndView("manage-users");
        view.addObject("users", adminService.getAllUsers());

        return view;
    }

    @PostMapping("/user/promote/{id}")
    public ModelAndView promote(@PathVariable("id") long id) {
        adminService.promote(id);

        return new ModelAndView("redirect:/admin/users");
    }

    @PostMapping("/user/demote/{id}")
    public ModelAndView demote(@PathVariable("id") long id) {
        adminService.demote(id);

        return new ModelAndView("redirect:/admin/users");
    }

    @PostMapping("/user/delete/{id}")
    public ModelAndView deleteUser(@PathVariable("id") long id) {
        adminService.deleteUser(id);

        return new ModelAndView("redirect:/admin/users");
    }

    @GetMapping("/games")
    public ModelAndView games() {
        ModelAndView view = new ModelAndView("manage-games");
        view.addObject("games", adminService.getAllGames());

        return view;
    }

    @GetMapping("/add-game")
    public ModelAndView addGame(Model model) {
        ModelAndView modelAndView = new ModelAndView("add-game");
        Map<GenreName, String> genreDescriptions = GenreConverter.getAllGenreDescriptions();
        modelAndView.addObject("genres", genreDescriptions);

        if (!model.containsAttribute("addGameBindingModel")) {
            model.addAttribute("addGameBindingModel", new AddGameBindingModel());
        }

        return modelAndView;
    }

    @PostMapping("/add-game")
    public ModelAndView addGame(@Valid AddGameBindingModel addGameBindingModel,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("addGameBindingModel", addGameBindingModel);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.addGameBindingModel", bindingResult);

            // handle errors
            return new ModelAndView("redirect:/admin/add-game");
        }

        adminService.addGame(addGameBindingModel);

        return new ModelAndView("redirect:/admin/games");
    }

    @PostMapping("/game/delete/{id}")
    public ModelAndView deleteGame(@PathVariable("id") Long id) {
        adminService.deleteGame(id);

        return new ModelAndView("redirect:/admin/games");
    }

    @GetMapping("/game/edit/{id}")
    public ModelAndView editGame(@PathVariable("id") Long id) {
        ModelAndView modelAndView = new ModelAndView("edit-game");
        UpdateGameBindingModel bindingModel = adminService.getById(id);

        modelAndView.addObject("bindingModel", bindingModel);
        modelAndView.addObject("genres", GenreConverter.getAllGenreDescriptions());

        return modelAndView;
    }

    @PostMapping("/game/edit/{id}")
    public ModelAndView editGame(@Valid UpdateGameBindingModel updateGameBindingModel,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes, @PathVariable Long id) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("editGameBindingModel", updateGameBindingModel);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.editGameBindingModel", bindingResult);

            // handle errors
            return new ModelAndView("redirect:/admin/game/edit/{id}");
        }

        adminService.editGame(updateGameBindingModel, id);

        return new ModelAndView("redirect:/admin/games");
    }
}
