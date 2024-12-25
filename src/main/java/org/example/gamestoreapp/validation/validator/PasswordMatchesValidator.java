package org.example.gamestoreapp.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.example.gamestoreapp.model.dto.ChangePasswordBindingModel;
import org.example.gamestoreapp.model.dto.ResetPasswordDTO;
import org.example.gamestoreapp.model.dto.UserRegisterBindingModel;
import org.example.gamestoreapp.validation.annotation.PasswordMatches;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, Object> {
    private String message;

    @Override
    public void initialize(PasswordMatches constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        this.message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext constraintValidatorContext) {
        String password = null;
        String confirmPassword = null;

        if (obj instanceof UserRegisterBindingModel userRegisterBindingModel) {
            password = userRegisterBindingModel.getPassword();
            confirmPassword = userRegisterBindingModel.getConfirmPassword();
        } else if (obj instanceof ChangePasswordBindingModel changePasswordBindingModel) {
            password = changePasswordBindingModel.getNewPassword();
            confirmPassword = changePasswordBindingModel.getConfirmPassword();
        } else if (obj instanceof ResetPasswordDTO resetPasswordDTO) {
            password = resetPasswordDTO.getNewPassword();
            confirmPassword = resetPasswordDTO.getConfirmPassword();
        }

        if (password == null || confirmPassword == null) {
            return false;
        }

        boolean isValid = password.equals(confirmPassword);

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
