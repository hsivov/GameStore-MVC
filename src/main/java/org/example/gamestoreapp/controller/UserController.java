package org.example.gamestoreapp.controller;

import jakarta.validation.Valid;
import org.example.gamestoreapp.model.dto.UserLoginBindingModel;
import org.example.gamestoreapp.model.dto.UserProfileViewModel;
import org.example.gamestoreapp.model.dto.UserRegisterBindingModel;
import org.example.gamestoreapp.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public ModelAndView login(@ModelAttribute("userLoginBindingModel") UserLoginBindingModel userLoginBindingModel) {

        return new ModelAndView("login");
    }

    @GetMapping("/register")
    public ModelAndView register(@ModelAttribute("userRegisterBindingModel") UserRegisterBindingModel userRegisterBindingModel) {

        return new ModelAndView("register");
    }

    @PostMapping("/register")
    public ModelAndView register(@ModelAttribute("userRegisterBindingModel") @Valid UserRegisterBindingModel userRegisterBindingModel,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("userRegisterBindingModel", userRegisterBindingModel);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.userRegisterBindingModel", bindingResult);

            // handle errors
            return new ModelAndView("register");
        }

        boolean hasSuccessfulRegistration = userService.register(userRegisterBindingModel);

        if (!hasSuccessfulRegistration){

            return new ModelAndView("register");
        }

        // register user
        return new ModelAndView("redirect:/users/login");
    }

    @GetMapping("/profile")
    public ModelAndView profile() {
        ModelAndView modelAndView = new ModelAndView("profile");

        UserProfileViewModel userProfileViewModel = userService.viewProfile();
        modelAndView.addObject("userProfileViewModel", userProfileViewModel);

        return modelAndView;
    }
}
