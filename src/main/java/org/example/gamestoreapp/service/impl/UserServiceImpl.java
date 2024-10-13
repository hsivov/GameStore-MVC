package org.example.gamestoreapp.service.impl;

import jakarta.mail.MessagingException;
import org.example.gamestoreapp.exception.TokenExpiredException;
import org.example.gamestoreapp.model.entity.ConfirmationToken;
import org.example.gamestoreapp.model.view.UserProfileViewModel;
import org.example.gamestoreapp.model.dto.UserRegisterBindingModel;
import org.example.gamestoreapp.model.entity.User;
import org.example.gamestoreapp.model.enums.UserRole;
import org.example.gamestoreapp.repository.UserRepository;
import org.example.gamestoreapp.service.ConfirmationTokenService;
import org.example.gamestoreapp.service.EmailService;
import org.example.gamestoreapp.service.UserService;
import org.example.gamestoreapp.service.session.UserHelperService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.mail.MailException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserHelperService userHelperService;
    private final ConfirmationTokenService tokenService;
    private final EmailService emailService;
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, UserHelperService userHelperService, ConfirmationTokenService tokenService, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userHelperService = userHelperService;
        this.tokenService = tokenService;
        this.emailService = emailService;
    }

    @Override
    public boolean register(UserRegisterBindingModel userRegisterBindingModel) {
        try {
            User user = new User();
            user.setUsername(userRegisterBindingModel.getUsername());
            user.setEmail(userRegisterBindingModel.getEmail());
            user.setFirstName(userRegisterBindingModel.getFirstName());
            user.setLastName(userRegisterBindingModel.getLastName());
            user.setAge(userRegisterBindingModel.getAge());
            user.setPassword(passwordEncoder.encode(userRegisterBindingModel.getPassword()));
            user.setRole(UserRole.USER);

            userRepository.save(user);

            sendConfirmationEmail(user);

            return true;
        } catch (DataAccessException e) {
            logger.error("Database error occurred during user registration: {}", e.getMessage(), e);
            return false;
        } catch (MailException e) {
            logger.error("Failed to send confirmation email: {}", e.getMessage(), e);
            return false;
        } catch (Exception e) {
            // Use the logger for generic errors
            logger.error("Unexpected error occurred during registration: {}", e.getMessage(), e);
            return false;
        }
    }

    private void sendConfirmationEmail(User user) throws MessagingException {
        ConfirmationToken token = new ConfirmationToken(user);

        tokenService.saveConfirmationToken(token);

        // Send confirmation email
        String link = "http://localhost:8080/users/confirm?token=" + token.getToken();

        String subject = "Confirm your email";
        String htmlContent = "<h3>Thank you for registering!</h3>"
                + "<p>Please click the link below to confirm your email:</p>"
                + "<a href='" + link + "'>Confirm Email</a>"
                + "<p>If you didn't request this, please ignore this email.</p>";

        emailService.sendEmail(user.getEmail(), subject, htmlContent);
    }

    @Override
    public void confirmToken(String token) {
        ConfirmationToken confirmationToken = tokenService.getToken(token)
                .orElseThrow(() -> new IllegalStateException("Token not found"));

        if (confirmationToken.getConfirmedAt() != null) {
            throw new IllegalStateException("Email already confirmed");
        }

        LocalDateTime expiredAt = confirmationToken.getExpiresAt();

        if (expiredAt.isBefore(LocalDateTime.now())) {
            throw new TokenExpiredException("Token expired");
        }

        // Mark token as confirmed
        confirmationToken.setConfirmedAt(LocalDateTime.now());
        tokenService.saveConfirmationToken(confirmationToken);

        // Enable the user account
        User user = confirmationToken.getUser();
        user.setEnabled(true);

        userRepository.save(user);
    }

    @Override
    public void resendConfirmationToken(String token) throws MessagingException {
        ConfirmationToken oldToken = tokenService.getToken(token)
                .orElseThrow(() -> new IllegalStateException("Invalid token"));

        User user = oldToken.getUser();

        if (user.isEnabled()) {
            throw new IllegalStateException("User already confirmed");
        }

        // Generate a new token and send the confirmation email
        sendConfirmationEmail(user);
    }

    @Override
    public UserProfileViewModel viewProfile() {
        User currentUser = userHelperService.getUser();
        UserProfileViewModel userProfileViewModel = new UserProfileViewModel();

        userProfileViewModel.setUsername(currentUser.getUsername());
        userProfileViewModel.setRole(currentUser.getRole().toString());
        userProfileViewModel.setAge(currentUser.getAge());
        userProfileViewModel.setFirstName(currentUser.getFirstName());
        userProfileViewModel.setLastName(currentUser.getLastName());

        return userProfileViewModel;
    }

    @Override
    public boolean isUniqueEmail(String email) {
        return !userRepository.existsByEmail(email);
    }

    @Override
    public boolean isUniqueUsername(String username) {
        return userRepository.findByUsername(username).isEmpty();
    }
}
