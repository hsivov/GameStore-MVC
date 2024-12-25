package org.example.gamestoreapp.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.example.gamestoreapp.service.AuthService;
import org.example.gamestoreapp.validation.annotation.CorrectPassword;

public class CorrectPasswordValidator implements ConstraintValidator<CorrectPassword, String> {
    private final AuthService authService;

    public CorrectPasswordValidator(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void initialize(CorrectPassword constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext constraintValidatorContext) {
        return authService.isCorrectPassword(password);
    }
}
