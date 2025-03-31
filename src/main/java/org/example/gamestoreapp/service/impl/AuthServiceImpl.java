package org.example.gamestoreapp.service.impl;

import jakarta.mail.MessagingException;
import org.example.gamestoreapp.exception.IllegalTokenException;
import org.example.gamestoreapp.exception.UsedTokenException;
import org.example.gamestoreapp.exception.UserNotFoundException;
import org.example.gamestoreapp.model.dto.ChangePasswordBindingModel;
import org.example.gamestoreapp.model.dto.ResetPasswordDTO;
import org.example.gamestoreapp.model.dto.UserRegisterBindingModel;
import org.example.gamestoreapp.model.entity.ConfirmationToken;
import org.example.gamestoreapp.model.entity.User;
import org.example.gamestoreapp.model.enums.UserRole;
import org.example.gamestoreapp.repository.ConfirmationTokenRepository;
import org.example.gamestoreapp.repository.UserRepository;
import org.example.gamestoreapp.service.AuthService;
import org.example.gamestoreapp.service.EmailService;
import org.example.gamestoreapp.service.TokenService;
import org.example.gamestoreapp.service.session.UserHelperService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.mail.MailException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final UserHelperService userHelperService;
    private final EmailService emailService;
    private final TokenService tokenService;
    private final ConfirmationTokenRepository tokenRepository;

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    @Value("${app.domain.name}")
    private String domain;

    public AuthServiceImpl(PasswordEncoder passwordEncoder,
                           UserRepository userRepository,
                           UserHelperService userHelperService,
                           EmailService emailService,
                           TokenService tokenService,
                           ConfirmationTokenRepository tokenRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.userHelperService = userHelperService;
        this.emailService = emailService;
        this.tokenService = tokenService;
        this.tokenRepository = tokenRepository;
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

    @Override
    public boolean isCorrectPassword(String password) {
        User currentUser = userHelperService.getUser();

        return passwordEncoder.matches(password, currentUser.getPassword());
    }

    @Override
    public void changePassword(ChangePasswordBindingModel changePasswordBindingModel) {
        User currentUser = userHelperService.getUser();

        currentUser.setPassword(passwordEncoder.encode(changePasswordBindingModel.getNewPassword()));
        userRepository.save(currentUser);
    }

    @Override
    public boolean isValidEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    @Override
    public void passwordResetRequest(String email) throws MessagingException {
        User requestedUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        sendResetPasswordEmail(requestedUser);
    }

    @Override
    public void resetPassword(ResetPasswordDTO resetPasswordDTO, String resetToken) {
        Optional<ConfirmationToken> tokenOptional = tokenRepository.findByToken(resetToken);

        if (tokenOptional.isPresent()) {
            ConfirmationToken token = tokenOptional.get();
            User requestedUser = token.getUser();

            requestedUser.setPassword(passwordEncoder.encode(resetPasswordDTO.getConfirmPassword()));
            userRepository.save(requestedUser);

            tokenService.invalidateToken(token);
        }
    }
  
    @Override
    public boolean isUniqueEmail(String email) {
        return userRepository.findByEmail(email).isEmpty();
    }

    @Override
    public boolean isUniqueUsername(String username) {
        return userRepository.findByUsername(username).isEmpty();
    }

    @Override
    public void resendConfirmationToken(String token) throws MessagingException {
        ConfirmationToken oldToken = tokenService.getToken(token)
                .orElseThrow(() -> new IllegalTokenException("Invalid token"));

        User user = oldToken.getUser();

        if (user.isEnabled()) {
            throw new UsedTokenException("Account already confirmed");
        }

        // Generate a new token and send the confirmation email
        sendConfirmationEmail(user);
    }

    @Override
    public void enableUser(String token) {
        Optional<ConfirmationToken> tokenOptional = tokenService.getToken(token);

        if (tokenOptional.isPresent()) {
            ConfirmationToken confirmationToken = tokenOptional.get();
            User user = confirmationToken.getUser();
            // Enable user account
            user.setEnabled(true);
            userRepository.save(user);

            tokenService.invalidateToken(confirmationToken); // Mark token as confirmed
        }
    }

    private void sendConfirmationEmail(User user) throws MessagingException {
        ConfirmationToken token = new ConfirmationToken(user);

        tokenService.saveConfirmationToken(token);

        // Send confirmation email
        String link = domain + "/auth/confirm?token=" + token.getToken();

        String subject = "Confirm your email";
        String htmlContent = "<h3>Thank you for registering!</h3>"
                + "<p>Please click the link below to confirm your email:</p>"
                + "<a href='" + link + "'>Confirm Email</a>"
                + "<p>If the button above doesn’t work, copy and paste the following link into your browser:</p>"
                + "<p>" + link + "</p>"
                + "<p>If you didn't request this, please ignore this email.</p>";

        emailService.sendEmail(user.getEmail(), subject, htmlContent);
    }

    private void sendResetPasswordEmail(User user) throws MessagingException {
        ConfirmationToken token = new ConfirmationToken(user);
        tokenRepository.save(token);

        String link = domain + "/auth/confirm/reset-password?token=" + token.getToken();

        String subject = "Reset your password";
        String htmlContent = "<p>Hello <strong>" + user.getFirstName() + "</strong>,</p>" +
                "<p>We received a request to reset the password for your <strong>" + user.getUsername() + "</strong> account. " +
                "If you made this request, please click the button below to reset your password:</p>" +
                "<a href=\"" + link + "\">Reset My Password</a>" +
                "<p>If the button above doesn’t work, copy and paste the following link into your browser:</p>" +
                "<p>" + link + "</p>" +
                "<p>This link is valid for <strong>15 minutes</strong>.</p>" +
                "<p><strong>If you did not request a password reset</strong>, no action is required. " +
                "Your account is still secure, and your password has not been changed. " +
                "If you suspect any suspicious activity, please contact our support team immediately.</p>" +
                "<p>Thank you,</p>" +
                "<p>The <strong>Game Store</strong> Support Team</p>" +
                "<div>" +
                "<p>This email is automatically generated. Please do not answer. If you need further assistance, " +
                "please contact us at <a href=\"mailto:support@yourwebsite.com\">support@yourwebsite.com</a>.</p>" +
                "</div>";

        emailService.sendEmail(user.getEmail(), subject, htmlContent);
    }
}
