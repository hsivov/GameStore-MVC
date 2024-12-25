package org.example.gamestoreapp.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.example.gamestoreapp.service.AuthService;
import org.example.gamestoreapp.validation.annotation.ValidEmail;

public class ValidEmailValidator implements ConstraintValidator<ValidEmail, String> {
    private final AuthService authService;

    public ValidEmailValidator(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void initialize(ValidEmail constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String email, ConstraintValidatorContext constraintValidatorContext) {
        return authService.isValidEmail(email);
    }
}
