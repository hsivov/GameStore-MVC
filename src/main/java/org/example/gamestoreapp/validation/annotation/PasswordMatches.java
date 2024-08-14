package org.example.gamestoreapp.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.example.gamestoreapp.validation.validator.PasswordMatchesValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Constraint(validatedBy = PasswordMatchesValidator.class)
public @interface PasswordMatches {
    String message() default "Password does not match";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
