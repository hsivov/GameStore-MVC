package org.example.gamestoreapp.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {

        String errorMessage = "Invalid username or password";

        if (exception.getMessage().equals("User is disabled")) {
            errorMessage = "Your account is not enabled. Please confirm your email.";
        }

        // Redirect to the login page with an error message
        response.sendRedirect("/users/login?error=" + errorMessage);
    }
}
