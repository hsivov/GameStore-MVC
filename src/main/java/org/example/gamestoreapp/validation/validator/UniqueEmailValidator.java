package org.example.gamestoreapp.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.example.gamestoreapp.model.entity.User;
import org.example.gamestoreapp.service.UserService;
import org.example.gamestoreapp.service.session.UserHelperService;
import org.example.gamestoreapp.validation.annotation.UniqueEmail;

public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, String> {
    private final UserService userService;
    private final UserHelperService userHelperService;

    public UniqueEmailValidator(UserService userService, UserHelperService userHelperService) {
        this.userService = userService;
        this.userHelperService = userHelperService;
    }

    @Override
    public void initialize(UniqueEmail constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String email, ConstraintValidatorContext constraintValidatorContext) {
        User user = userHelperService.getUser();

        if (user != null && user.getEmail().equals(email)) {
            return true;
        }

        return userService.isUniqueEmail(email);
    }
}
