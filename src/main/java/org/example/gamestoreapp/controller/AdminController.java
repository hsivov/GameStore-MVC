package org.example.gamestoreapp.controller;

import jakarta.validation.Valid;
import org.example.gamestoreapp.model.dto.*;
import org.example.gamestoreapp.service.AdminService;
import org.example.gamestoreapp.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final AdminService adminService;
    private final OrderService orderService;

    public AdminController(AdminService adminService, OrderService orderService) {
        this.adminService = adminService;
        this.orderService = orderService;
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

    @PostMapping("/user/change/{id}")
    public ModelAndView changeUserStatus(@PathVariable("id") long id) {
        adminService.toggleUserState(id);

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
        ModelAndView modelAndView = new ModelAndView("add_game");
        List<GenreDTO> genres = adminService.getAllGenres();
        modelAndView.addObject("genres", genres);

        if (!model.containsAttribute("addGameBindingModel")) {
            model.addAttribute("addGameBindingModel", new AddGameBindingModel());
        }

        return modelAndView;
    }

    @PostMapping("/add-game")
    public ModelAndView addGame(@Valid AddGameBindingModel addGameBindingModel,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) throws IOException {

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
        UpdateGameBindingModel bindingModel = adminService.getGameById(id);

        modelAndView.addObject("bindingModel", bindingModel);
        modelAndView.addObject("genres", adminService.getAllGenres());

        return modelAndView;
    }

    @PostMapping("/game/edit")
    public ModelAndView editGame(@Valid UpdateGameBindingModel updateGameBindingModel,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) throws IOException {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("updateGameBindingModel", updateGameBindingModel);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.updateGameBindingModel", bindingResult);

            // handle errors
            return new ModelAndView("redirect:/admin/game/edit/" + updateGameBindingModel.getId());
        }

        adminService.editGame(updateGameBindingModel);

        return new ModelAndView("redirect:/admin/games");
    }

    @GetMapping("/genres")
    public String genres(Model model) {
        List<GenreDTO> genres = adminService.getAllGenres();
        model.addAttribute("genres", genres);

        return "manage-genres";
    }

    @GetMapping("/add-genre")
    public ModelAndView addGenre(Model model) {
        if (!model.containsAttribute("addGenreBindingModel")) {
            model.addAttribute("addGenreBindingModel", new AddGenreBindingModel());
        }

        return new ModelAndView("add_genre");
    }

    @PostMapping("/add-genre")
    public ModelAndView addGenre(@Valid AddGenreBindingModel addGenreBindingModel, BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("addGenreBindingModel", addGenreBindingModel);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.addGenreBindingModel", bindingResult);

            return new ModelAndView("redirect:/admin/add-genre");
        }

        adminService.addGenre(addGenreBindingModel);

        return new ModelAndView("redirect:/admin/genres");
    }

    @GetMapping("/genre/edit/{id}")
    public ModelAndView editGenre(@PathVariable Long id, Model model) {
        if (!model.containsAttribute("updateGenreBindingModel")) {
            model.addAttribute("updateGenreBindingModel", adminService.getGenreById(id));
        }

        return new ModelAndView("edit-genre");
    }

    @PostMapping("/genre/edit")
    public ModelAndView editGenre(@Valid UpdateGenreBindingModel updateGenreBindingModel,
                                  BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("updateGenreBindingModel", updateGenreBindingModel);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.updateGenreBindingModel", bindingResult);

            return new ModelAndView("redirect:/admin/genre/edit/" + updateGenreBindingModel.getId());
        }

        adminService.editGenre(updateGenreBindingModel);

        return new ModelAndView("redirect:/admin/genres");
    }

    @DeleteMapping("/genre/delete/{id}")
    public ModelAndView deleteGenre(@PathVariable Long id) {
        adminService.deleteGenre(id);

        return new ModelAndView("redirect:/admin/genres");
    }

    @GetMapping("/orders")
    public String getOrders(Model model) {
        List<OrderResponseDTO> allOrders = orderService.getAllOrders();

        model.addAttribute("orders", allOrders);

        return "order-list";
    }

    @GetMapping("/order/{orderId}")
    public String getOrderDetails(@PathVariable("orderId") long id, Model model) {
        OrderResponseDTO order = orderService.getOrderById(id);
        model.addAttribute("order", order);
        return "order-details";
    }
}
