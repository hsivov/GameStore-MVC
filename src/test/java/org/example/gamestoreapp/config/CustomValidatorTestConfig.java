package org.example.gamestoreapp.config;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorFactory;

import org.example.gamestoreapp.service.AuthService;
import org.example.gamestoreapp.service.session.UserHelperService;
import org.example.gamestoreapp.validation.validator.UniqueEmailValidator;
import org.example.gamestoreapp.validation.validator.UniqueUsernameValidator;
import org.example.gamestoreapp.validation.validator.ValidEmailValidator;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

@TestConfiguration
public class CustomValidatorTestConfig {

    @Bean
    public Validator validator(AuthService authService, UserHelperService userHelperService) {
        ConstraintValidatorFactory constraintValidatorFactory = new ConstraintValidatorFactory() {

            @Override
            @SuppressWarnings("unchecked")
            public <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> key) {
                try {
                    if (key == UniqueEmailValidator.class) {
                        return (T) new UniqueEmailValidator(authService, userHelperService);
                    }
                    if (key == UniqueUsernameValidator.class) {
                        return (T) new UniqueUsernameValidator(authService);
                    }
                    if (key == ValidEmailValidator.class) {
                        return (T) new ValidEmailValidator(authService);
                    }
                    return key.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    throw new RuntimeException("Failed to instantiate validator", e);
                }
            }

            @Override
            public void releaseInstance(ConstraintValidator<?, ?> instance) {
                // No-op
            }
        };

        LocalValidatorFactoryBean factoryBean = new LocalValidatorFactoryBean();
        factoryBean.setConstraintValidatorFactory(constraintValidatorFactory);
        factoryBean.afterPropertiesSet(); // Initialize the validator

        return factoryBean;
    }

    @Bean
    public ViewResolver viewResolver() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();

        viewResolver.setPrefix("classpath:templates/");
        viewResolver.setSuffix(".html");

        return viewResolver;
    }
}

