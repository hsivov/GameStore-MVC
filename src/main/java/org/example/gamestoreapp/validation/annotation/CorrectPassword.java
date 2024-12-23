package org.example.gamestoreapp.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.example.gamestoreapp.validation.validator.CorrectPasswordValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Constraint(validatedBy = CorrectPasswordValidator.class)
public @interface CorrectPassword {
    String message() default "Provided password is incorrect";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
