package org.example.gamestoreapp.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.example.gamestoreapp.service.UserService;
import org.example.gamestoreapp.validation.annotation.CorrectPassword;

public class CorrectPasswordValidator implements ConstraintValidator<CorrectPassword, String> {
    private final UserService userService;

    public CorrectPasswordValidator(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void initialize(CorrectPassword constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext constraintValidatorContext) {
        return userService.isCorrectPassword(password);
    }
}
