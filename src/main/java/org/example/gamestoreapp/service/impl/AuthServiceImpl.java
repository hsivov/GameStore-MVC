package org.example.gamestoreapp.service.impl;

import org.example.gamestoreapp.event.*;
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
import org.example.gamestoreapp.service.TokenService;
import org.example.gamestoreapp.service.session.UserHelperService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
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
    private final TokenService tokenService;
    private final ConfirmationTokenRepository tokenRepository;
    private final ApplicationEventPublisher eventPublisher;

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    public AuthServiceImpl(PasswordEncoder passwordEncoder,
                           UserRepository userRepository,
                           UserHelperService userHelperService,
                           TokenService tokenService,
                           ConfirmationTokenRepository tokenRepository,
                           ApplicationEventPublisher eventPublisher) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.userHelperService = userHelperService;
        this.tokenService = tokenService;
        this.tokenRepository = tokenRepository;
        this.eventPublisher = eventPublisher;
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

            eventPublisher.publishEvent(new UserRegisteredEvent(user));

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

        eventPublisher.publishEvent(new PasswordChangedEvent(currentUser));
    }

    @Override
    public boolean isValidEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    @Override
    public void passwordResetRequest(String email) {
        User requestedUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        eventPublisher.publishEvent(new PasswordResetRequestEvent(requestedUser));
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

            eventPublisher.publishEvent(new PasswordResetEvent(requestedUser));
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
    public void resendConfirmationToken(String token) {
        ConfirmationToken oldToken = tokenService.getToken(token)
                .orElseThrow(() -> new IllegalTokenException("Invalid token"));

        User user = oldToken.getUser();

        if (user.isEnabled()) {
            throw new UsedTokenException("Account already confirmed");
        }

        // Generate a new token and send the confirmation email
        eventPublisher.publishEvent(new UserRegisteredEvent(user));
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

            eventPublisher.publishEvent(new UserEnabledEvent(user));
        }
    }
}
