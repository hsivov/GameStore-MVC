package org.example.gamestoreapp.controller;

import org.example.gamestoreapp.config.CustomValidatorTestConfig;
import org.example.gamestoreapp.exception.IllegalTokenException;
import org.example.gamestoreapp.exception.TokenExpiredException;
import org.example.gamestoreapp.exception.UsedTokenException;
import org.example.gamestoreapp.model.dto.ForgotPasswordDTO;
import org.example.gamestoreapp.model.dto.ResetPasswordDTO;
import org.example.gamestoreapp.model.dto.UserRegisterBindingModel;
import org.example.gamestoreapp.service.AuthService;
import org.example.gamestoreapp.service.TokenService;
import org.example.gamestoreapp.service.session.UserHelperService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.Validator;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ViewResolver;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @Mock
    private TokenService tokenService;

    @Mock
    private UserHelperService userHelperService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        Validator validator = new CustomValidatorTestConfig().validator(authService, userHelperService);
        ViewResolver viewResolver = new CustomValidatorTestConfig().viewResolver();

        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .setViewResolvers(viewResolver)
                .setValidator(validator)
                .build();
    }

    @Test
    void testLoginNotConfirmed() throws Exception {
        mockMvc.perform(get("/auth/login"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("userLoginBindingModel"))
                .andExpect(view().name("login"));
    }

    @Test
    void testLoginConfirmed() throws Exception {
        mockMvc.perform(get("/auth/login").param("confirmed", "true"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("userLoginBindingModel"))
                .andExpect(model().attributeExists("message"))
                .andExpect(model().attribute("message", "Your account has been confirmed"))
                .andExpect(view().name("login"));
    }

    @Test
    void testRegisterModelNotExists() throws Exception {
        mockMvc.perform(get("/auth/register"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("userRegisterBindingModel"))
                .andExpect(view().name("register"));
    }

    @Test
    void testRegisterModelExists() throws Exception {
        UserRegisterBindingModel existingModel = new UserRegisterBindingModel();
        existingModel.setUsername("username");

        mockMvc.perform(get("/auth/register")
                        .flashAttr("userRegisterBindingModel", existingModel))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("userRegisterBindingModel"))
                .andExpect(model().attribute("userRegisterBindingModel", existingModel))
                .andExpect(view().name("register"));
    }

    @Test
    void testRegisterWithBindingErrors() throws Exception {
        UserRegisterBindingModel invalidModel = new UserRegisterBindingModel();
        invalidModel.setUsername("");

        mockMvc.perform(post("/auth/register")
                        .flashAttr("userRegisterBindingModel", invalidModel))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/register"))
                .andExpect(flash().attributeExists("userRegisterBindingModel"))
                .andExpect(flash().attributeExists("org.springframework.validation.BindingResult.userRegisterBindingModel"));
    }

    @Test
    void testRegister_Success() throws Exception {
        UserRegisterBindingModel validModel = new UserRegisterBindingModel();
        validModel.setUsername("username");
        validModel.setPassword("password");
        validModel.setConfirmPassword("password");
        validModel.setEmail("email@example.com");
        validModel.setFirstName("John");
        validModel.setLastName("Doe");
        validModel.setAge(25);

        when(authService.isUniqueEmail("email@example.com")).thenReturn(true);
        when(authService.isUniqueUsername("username")).thenReturn(true);
        when(authService.register(validModel)).thenReturn(true);

        mockMvc.perform(post("/auth/register")
                        .flashAttr("userRegisterBindingModel", validModel))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login"))
                .andExpect(flash().attribute("message", "Registration successful! Please check your email to confirm."));

        verify(authService, times(1)).isUniqueEmail("email@example.com");
        verify(authService, times(1)).isUniqueUsername("username");
        verify(authService, times(1)).register(validModel);
    }

    @Test
    void testRegister_Failed() throws Exception {
        UserRegisterBindingModel validModel = new UserRegisterBindingModel();
        validModel.setUsername("username");
        validModel.setPassword("password");
        validModel.setConfirmPassword("password");
        validModel.setEmail("email@example.com");
        validModel.setFirstName("John");
        validModel.setLastName("Doe");
        validModel.setAge(25);

        when(authService.isUniqueEmail("email@example.com")).thenReturn(true);
        when(authService.isUniqueUsername("username")).thenReturn(true);
        when(authService.register(validModel)).thenReturn(false);

        mockMvc.perform(post("/auth/register")
                        .flashAttr("userRegisterBindingModel", validModel))
                .andExpect(status().is5xxServerError())
                .andExpect(model().attribute("error", "Registration failed, please try again."))
                .andExpect(view().name("register"));

        verify(authService, times(1)).isUniqueEmail("email@example.com");
        verify(authService, times(1)).isUniqueUsername("username");
        verify(authService, times(1)).register(validModel);
    }

    @Test
    void testConfirmEmail_WhenTokenIsValid_ShouldRedirectToLoginWithConfirmedParam() throws Exception {
        // Arrange
        String token = "validToken";

        // Act & Assert
        mockMvc.perform(get("/auth/confirm").param("token", token))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login?confirmed"));

        // Verify interactions
        verify(tokenService, times(1)).verifyToken(token);
        verify(authService, times(1)).enableUser(token);
    }

    @Test
    void testConfirmEmail_WhenTokenIsExpired_ShouldRedirectToTokenExpired() throws Exception {
        // Arrange
        String token = "expiredToken";
        doThrow(new TokenExpiredException("Token has expired")).when(tokenService).verifyToken(token);

        // Act & Assert
        mockMvc.perform(get("/auth/confirm").param("token", token))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/token-expired"))
                .andExpect(flash().attribute("message", "Token has expired"))
                .andExpect(flash().attribute("token", token));

        // Verify interactions
        verify(tokenService).verifyToken(token);
        verifyNoInteractions(authService); // Should not enable user if token expired
    }

    @Test
    void testConfirmEmail_WhenTokenIsAlreadyUsed_ShouldRedirectToLogin() throws Exception {
        // Arrange
        String token = "usedToken";
        doThrow(new UsedTokenException("Token already used")).when(tokenService).verifyToken(token);

        // Act & Assert
        mockMvc.perform(get("/auth/confirm").param("token", token))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login"))
                .andExpect(flash().attribute("message", "Token already used"));

        // Verify interactions
        verify(tokenService).verifyToken(token);
        verifyNoInteractions(authService); // Should not enable user if token already used
    }

    @Test
    void testConfirmEmail_WhenUnexpectedExceptionOccurs_ShouldRedirectToErrorPage() throws Exception {
        // Arrange
        String token = "unexpectedToken";
        doThrow(new RuntimeException("Unexpected error")).when(tokenService).verifyToken(token);

        // Act & Assert
        mockMvc.perform(get("/auth/confirm").param("token", token))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/error"))
                .andExpect(flash().attribute("message", "Unexpected error"));

        // Verify interactions
        verify(tokenService).verifyToken(token);
        verifyNoInteractions(authService);
    }

    @Test
    void testResendConfirmationToken() throws Exception {
        String token = "validToken";

        mockMvc.perform(post("/auth/resend-confirmation")
                        .param("token", token))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login?resendSuccess"));
    }

    @Test
    void testResendConfirmationToken_WhenTokenIsUsed_ShouldRedirectToLogin() throws Exception {
        String token = "expiredToken";
        doThrow(new UsedTokenException("Token already used")).when(authService).resendConfirmationToken(token);

        mockMvc.perform(post("/auth/resend-confirmation")
                        .param("token", token))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login"))
                .andExpect(flash().attribute("message", "Token already used"));
    }

    @Test
    void testTokenExpiredPageEndPoint() throws Exception {
        mockMvc.perform(get("/auth/token-expired"))
                .andExpect(status().isOk())
                .andExpect(view().name("token-expired"));
    }

    @Test
    void testForgotPassword_ModelDoesNotExist() throws Exception {
        mockMvc.perform(get("/auth/forgotten-password"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("forgotPasswordDTO"))
                .andExpect(model().attribute("message", ""))
                .andExpect(view().name("forgotten-password"));
    }

    @Test
    void testForgotPassword_ModelExist() throws Exception {
        ForgotPasswordDTO existingModel = new ForgotPasswordDTO();
        existingModel.setEmail("email@example.com");


        mockMvc.perform(get("/auth/forgotten-password")
                        .flashAttr("forgotPasswordDTO", existingModel))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("forgotPasswordDTO"))
                .andExpect(model().attribute("forgotPasswordDTO", existingModel))
                .andExpect(model().attribute("message", ""))
                .andExpect(view().name("forgotten-password"));
    }

    @Test
    void testForgotPasswordEndPoint_ModelExist_And_ModelHasMessage() throws Exception {
        ForgotPasswordDTO existingModel = new ForgotPasswordDTO();
        existingModel.setEmail("email@example.com");

        String message = "An email has been sent to you with instructions how to reset your password.";

        mockMvc.perform(get("/auth/forgotten-password")
                        .flashAttr("forgotPasswordDTO", existingModel)
                        .param("message", message))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("forgotPasswordDTO"))
                .andExpect(model().attribute("forgotPasswordDTO", existingModel))
                .andExpect(model().attributeExists("message"))
                .andExpect(model().attribute("message", message))
                .andExpect(view().name("forgotten-password"));
    }

    @Test
    void testForgotPassword_WithBindingErrors() throws Exception {
        mockMvc.perform(post("/auth/forgotten-password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/forgotten-password"))
                .andExpect(flash().attributeExists("forgotPasswordDTO"))
                .andExpect(flash().attributeExists("org.springframework.validation.BindingResult.forgotPasswordDTO"));
    }

    @Test
    void testForgotPassword_SubmitSuccess() throws Exception {
        ForgotPasswordDTO validModel = new ForgotPasswordDTO();
        validModel.setEmail("email@example.com");

        when(authService.isValidEmail(validModel.getEmail())).thenReturn(true);

        mockMvc.perform(post("/auth/forgotten-password")
                        .flashAttr("forgotPasswordDTO", validModel))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/forgotten-password"))
                .andExpect(flash().attribute("message", "An email has been sent to you with instructions how to reset your password."));

        verify(authService, times(1)).isValidEmail(validModel.getEmail());
        verify(authService, times(1)).passwordResetRequest(validModel.getEmail());
    }

    @Test
    void testConfirmResetPassword_ValidToken() throws Exception {
        String token = "validToken123";

        mockMvc.perform(get("/auth/confirm/reset-password")
                        .param("token", token))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/reset-password?token=" + token));

        verify(tokenService, times(1)).verifyToken(token);
    }

    @Test
    void testConfirmResetPassword_InvalidToken() throws Exception {
        String invalidToken = "!@#123";

        doThrow(new IllegalTokenException("Invalid token")).when(tokenService).verifyToken(invalidToken);

        mockMvc.perform(get("/auth/confirm/reset-password")
                        .param("token", invalidToken))
                .andExpect(status().isUnauthorized())
                .andExpect(result -> assertInstanceOf(ResponseStatusException.class, result.getResolvedException()))
                .andExpect(result -> assertEquals("401 UNAUTHORIZED \"The token is invalid or has expired.\"",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));

        verify(tokenService, times(1)).verifyToken(invalidToken);
    }

    @Test
    void testConfirmResetPassword_UsedToken() throws Exception {
        String usedToken = "usedToken";

        doThrow(new UsedTokenException("Used token")).when(tokenService).verifyToken(usedToken);

        mockMvc.perform(get("/auth/confirm/reset-password")
                        .param("token", usedToken))
                .andExpect(status().isUnauthorized())
                .andExpect(result -> assertInstanceOf(ResponseStatusException.class, result.getResolvedException()))
                .andExpect(result -> assertEquals("401 UNAUTHORIZED \"The token is invalid or has expired.\"",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));

        verify(tokenService, times(1)).verifyToken(usedToken);
    }

    @Test
    void testConfirmResetPassword_ExpiredToken() throws Exception {
        String expiredToken = "expiredToken";

        doThrow(new TokenExpiredException("Expired token")).when(tokenService).verifyToken(expiredToken);

        mockMvc.perform(get("/auth/confirm/reset-password")
                        .param("token", expiredToken))
                .andExpect(status().isUnauthorized())
                .andExpect(result -> assertInstanceOf(ResponseStatusException.class, result.getResolvedException()))
                .andExpect(result -> assertEquals("401 UNAUTHORIZED \"The token is invalid or has expired.\"",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));

        verify(tokenService, times(1)).verifyToken(expiredToken);
    }

    @Test
    void testResetPassword_ValidRequest_ModelDoesNotExists() throws Exception {
        mockMvc.perform(get("/auth/reset-password")
                        .param("token", "validToken")
                        .param("message", "Password reset successful.")
                        .param("errorMessage", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("reset-password"))
                .andExpect(model().attributeExists("resetPasswordDTO"))
                .andExpect(model().attribute("token", "validToken"))
                .andExpect(model().attribute("message", "Password reset successful."))
                .andExpect(model().attribute("errorMessage", ""));
    }

    @Test
    void testResetPassword_ValidRequest_ModelExists() throws Exception {
        ResetPasswordDTO existingModel = new ResetPasswordDTO();
        existingModel.setNewPassword("newPassword");
        existingModel.setConfirmPassword("confirmPassword");

        mockMvc.perform(get("/auth/reset-password")
                        .flashAttr("resetPasswordDTO", existingModel)
                        .param("token", "validToken")
                        .param("message", "Password reset successful.")
                        .param("errorMessage", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("reset-password"))
                .andExpect(model().attribute("resetPasswordDTO", existingModel))
                .andExpect(model().attribute("token", "validToken"))
                .andExpect(model().attribute("message", "Password reset successful."))
                .andExpect(model().attribute("errorMessage", ""));
    }

    @Test
    void testResetPassword_InvalidRequest() throws Exception {
        mockMvc.perform(get("/auth/reset-password"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testResetPassword_SubmitSuccess() throws Exception {
        ResetPasswordDTO validModel = new ResetPasswordDTO();
        validModel.setNewPassword("newPassword");
        validModel.setConfirmPassword("newPassword");

        String token = "validToken123";

        mockMvc.perform(post("/auth/reset-password")
                        .flashAttr("resetPasswordDTO", validModel)
                        .param("token", token))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login"))
                .andExpect(flash().attribute("message", "Your password has been reset. You can now log in with your new password."));

        verify(tokenService, times(1)).verifyToken(token);
        verify(authService, times(1)).resetPassword(validModel, token);
    }

    @Test
    void testResetPassword_WithBindingErrors() throws Exception {
        ResetPasswordDTO invalidModel = new ResetPasswordDTO();
        invalidModel.setNewPassword("");

        String token = "validToken123";

        mockMvc.perform(post("/auth/reset-password")
                        .flashAttr("resetPasswordDTO", invalidModel)
                        .param("token", token))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/reset-password?token=" + token))
                .andExpect(flash().attribute("resetPasswordDTO", invalidModel))
                .andExpect(flash().attributeExists("org.springframework.validation.BindingResult.resetPasswordDTO"));
    }

    @Test
    void testResetPassword_InvalidToken() throws Exception {
        ResetPasswordDTO validModel = new ResetPasswordDTO();
        validModel.setNewPassword("newPassword");
        validModel.setConfirmPassword("newPassword");

        String invalidToken = "!@#123";

        doThrow(new IllegalTokenException("Invalid token")).when(tokenService).verifyToken(invalidToken);

        mockMvc.perform(post("/auth/reset-password")
                        .flashAttr("resetPasswordDTO", validModel)
                        .param("token", invalidToken))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/reset-password?token=" + invalidToken));

        verify(tokenService, times(1)).verifyToken(invalidToken);
    }

    @Test
    void testResetPassword_UsedToken() throws Exception {
        ResetPasswordDTO validModel = new ResetPasswordDTO();
        validModel.setNewPassword("newPassword");
        validModel.setConfirmPassword("newPassword");

        String usedToken = "usedToken123";

        doThrow(new UsedTokenException("Used token")).when(tokenService).verifyToken(usedToken);

        mockMvc.perform(post("/auth/reset-password")
                        .flashAttr("resetPasswordDTO", validModel)
                        .param("token", usedToken))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/reset-password?token=" + usedToken));

        verify(tokenService, times(1)).verifyToken(usedToken);
    }

    @Test
    void testResetPassword_ExpiredToken() throws Exception {
        ResetPasswordDTO validModel = new ResetPasswordDTO();
        validModel.setNewPassword("newPassword");
        validModel.setConfirmPassword("newPassword");

        String expiredToken = "expiredToken";

        doThrow(new TokenExpiredException("Expired token")).when(tokenService).verifyToken(expiredToken);

        mockMvc.perform(post("/auth/reset-password")
                        .flashAttr("resetPasswordDTO", validModel)
                        .param("token", expiredToken))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/reset-password?token=" + expiredToken));

        verify(tokenService, times(1)).verifyToken(expiredToken);
    }
}