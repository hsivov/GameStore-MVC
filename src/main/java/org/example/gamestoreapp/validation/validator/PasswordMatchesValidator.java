package org.example.gamestoreapp.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.example.gamestoreapp.model.dto.UserRegisterBindingModel;
import org.example.gamestoreapp.validation.annotation.PasswordMatches;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, UserRegisterBindingModel> {
    private String message;

    @Override
    public void initialize(PasswordMatches constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        this.message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(UserRegisterBindingModel bindingModel, ConstraintValidatorContext constraintValidatorContext) {
        if (bindingModel.getPassword() == null || bindingModel.getConfirmPassword() == null) {
            return true;
        }

        boolean isValid = bindingModel.getPassword().equals(bindingModel.getConfirmPassword());

        if (!isValid) {
            constraintValidatorContext.unwrap(HibernateConstraintValidatorContext.class)
                    .buildConstraintViolationWithTemplate(message)
                    .addPropertyNode("confirmPassword")
                    .addConstraintViolation()
                    .disableDefaultConstraintViolation();
        }

        return isValid;
    }
}
