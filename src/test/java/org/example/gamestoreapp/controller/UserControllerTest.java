package org.example.gamestoreapp.controller;

import org.example.gamestoreapp.config.TestConfig;
import org.example.gamestoreapp.model.dto.ChangePasswordBindingModel;
import org.example.gamestoreapp.model.dto.EditProfileDTO;
import org.example.gamestoreapp.model.dto.OrderResponseDTO;
import org.example.gamestoreapp.model.dto.ShoppingCartDTO;
import org.example.gamestoreapp.model.entity.User;
import org.example.gamestoreapp.model.view.UserProfileViewModel;
import org.example.gamestoreapp.service.*;
import org.example.gamestoreapp.service.session.UserHelperService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {
    @Mock
    private UserService userService;

    @Mock
    private GameService gameService;

    @Mock
    private ShoppingCartService shoppingCartService;

    @Mock
    private OrderService orderService;

    @Mock
    private AuthService authService;

    @Mock
    private UserHelperService userHelperService;

    @Mock
    private AzureBlobStorageService azureBlobStorageService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        TestConfig testConfig = new TestConfig();

        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setViewResolvers(testConfig.viewResolver())
                .setValidator(testConfig.validator(authService, userHelperService))
                .build();
    }

    @Test
    void testProfileEndpoint() throws Exception {
        UserProfileViewModel mockProfileView = new UserProfileViewModel();
        mockProfileView.setUsername("username");

        when(userService.getProfileView()).thenReturn(mockProfileView);

        mockMvc.perform(get("/user/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("userProfileViewModel"))
                .andExpect(model().attribute("userProfileViewModel", mockProfileView));

        verify(userService, times(1)).getProfileView();
    }

    @Test
    void testUploadImage_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "profileImage", "test.jpg", "image/jpeg", "test image content".getBytes()
        );

        String mockUrl = "https://azure.blob.storage/profile-images/test.jpg";

        when(azureBlobStorageService.uploadToAzureBlobStorage(any(MultipartFile.class), eq("profile-images"))).thenReturn(mockUrl);

        mockMvc.perform(multipart("/user/profile/upload-image")
                        .file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.profileImageUrl").value(mockUrl));

        verify(azureBlobStorageService, times(1)).uploadToAzureBlobStorage(any(MultipartFile.class), eq("profile-images"));
    }

    @Test
    void testUploadImage_Failure() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "profileImage", "test.jpg", "image/jpeg", "test image content".getBytes()
        );

        when(azureBlobStorageService.uploadToAzureBlobStorage(file, "profile-images"))
                .thenThrow(new IOException("Upload failed"));

        mockMvc.perform(multipart("/user/profile/upload-image")
                        .file(file))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Error uploading file")));

        verify(azureBlobStorageService, times(1)).uploadToAzureBlobStorage(any(MultipartFile.class), eq("profile-images"));
    }

    @Test
    void testEditProfile_WhenModelExists() throws Exception {
        EditProfileDTO existingModel = new EditProfileDTO();
        existingModel.setFirstName("Test");
        existingModel.setEmail("email@example.com");

        mockMvc.perform(get("/user/edit-profile")
                        .flashAttr("editProfileDTO", existingModel))
                .andExpect(status().isOk())
                .andExpect(view().name("edit-profile"))
                .andExpect(model().attribute("editProfileDTO", existingModel));
    }

    @Test
    void testEditProfile_WhenModeDoesNotExists() throws Exception {
        EditProfileDTO mockProfile = new EditProfileDTO();
        mockProfile.setFirstName("Test");
        mockProfile.setEmail("email@example.com");

        when(userService.getUserProfile()).thenReturn(mockProfile);

        mockMvc.perform(get("/user/edit-profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("edit-profile"))
                .andExpect(model().attribute("editProfileDTO", mockProfile));

        verify(userService, times(1)).getUserProfile();
    }

    @Test
    void testEditProfile_WithBindingErrors() throws Exception {
        mockMvc.perform(post("/user/edit-profile")
                        .param("email", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/edit-profile"))
                .andExpect(flash().attributeExists("editProfileDTO"))
                .andExpect(flash().attributeExists("org.springframework.validation.BindingResult.editProfileDTO"));
    }

    @Test
    void testEditProfile_SubmitSuccess() throws Exception {
        EditProfileDTO validModel = new EditProfileDTO();
        validModel.setEmail("email@example.com");
        validModel.setFirstName("First");
        validModel.setLastName("Last");
        validModel.setAge(25);

        when(authService.isUniqueEmail("email@example.com")).thenReturn(true);
        doNothing().when(userService).editProfile(validModel);

        mockMvc.perform(post("/user/edit-profile")
                        .flashAttr("editProfileDTO", validModel))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/profile"));

        verify(authService, times(1)).isUniqueEmail("email@example.com");
        verify(userService, times(1)).editProfile(validModel);
    }

    @Test
    void testChangePassword_ModelExists() throws Exception {
        ChangePasswordBindingModel existingModel = new ChangePasswordBindingModel();
        existingModel.setNewPassword("password");
        existingModel.setConfirmPassword("password");

        mockMvc.perform(get("/user/change-password")
                        .flashAttr("changePasswordBindingModel", existingModel)
                        .param("message", "Password has been changed successfully."))
                .andExpect(status().isOk())
                .andExpect(view().name("change-password"))
                .andExpect(model().attribute("message", "Password has been changed successfully."))
                .andExpect(model().attribute("changePasswordBindingModel", existingModel));
    }

    @Test
    void testChangePassword_ModelDoesNotExists() throws Exception {
        mockMvc.perform(get("/user/change-password"))
                .andExpect(status().isOk())
                .andExpect(view().name("change-password"))
                .andExpect(model().attribute("message", ""))
                .andExpect(model().attributeExists("changePasswordBindingModel"));
    }

    @Test
    void testChangePassword_WithBindingErrors() throws Exception {
        ChangePasswordBindingModel invalidModel = new ChangePasswordBindingModel();
        invalidModel.setNewPassword("password");
        invalidModel.setConfirmPassword("");

        mockMvc.perform(post("/user/change-password")
                        .flashAttr("changePasswordBindingModel", invalidModel))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/change-password"))
                .andExpect(flash().attribute("message", "Validation error!"))
                .andExpect(flash().attribute("changePasswordBindingModel", invalidModel))
                .andExpect(flash().attributeExists("org.springframework.validation.BindingResult.changePasswordBindingModel"));
    }

    @Test
    void testChangePassword_SubmitSuccess() throws Exception {
        ChangePasswordBindingModel validModel = new ChangePasswordBindingModel();
        validModel.setCurrentPassword("currentPassword");
        validModel.setNewPassword("newPassword");
        validModel.setConfirmPassword("newPassword");

        when(authService.isCorrectPassword(validModel.getCurrentPassword())).thenReturn(true);
        doNothing().when(authService).changePassword(validModel);

        mockMvc.perform(post("/user/change-password")
                        .flashAttr("changePasswordBindingModel", validModel))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/change-password"))
                .andExpect(flash().attribute("message", "Password has been changed successfully."));

        verify(authService, times(1)).isCorrectPassword(validModel.getCurrentPassword());
        verify(authService, times(1)).changePassword(validModel);
    }

    @Test
    void testShoppingCart() throws Exception {
        ShoppingCartDTO mockCart = new ShoppingCartDTO();
        mockCart.setTotalItems(3);

        when(shoppingCartService.getShoppingCart()).thenReturn(mockCart);

        mockMvc.perform(get("/user/shopping-cart"))
                .andExpect(status().isOk())
                .andExpect(view().name("shopping-cart"))
                .andExpect(model().attribute("shoppingCart", mockCart));

        verify(shoppingCartService, times(1)).getShoppingCart();
    }

    @Test
    void testAddToCartWithResponse() throws Exception {
        Long gameId = 1L;
        int updatedTotalItems = 5;

        ShoppingCartDTO mockShoppingCart = new ShoppingCartDTO();
        mockShoppingCart.setTotalItems(updatedTotalItems);

        doNothing().when(shoppingCartService).addToCart(gameId);

        when(shoppingCartService.getShoppingCart()).thenReturn(mockShoppingCart);

        mockMvc.perform(post("/user/add-to-cart/{id}", gameId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(updatedTotalItems));

        verify(shoppingCartService, times(1)).addToCart(gameId);
        verify(shoppingCartService, times(1)).getShoppingCart();
    }

    @Test
    void testAddToLibrary() throws Exception {
        Long gameId = 1L;

        doNothing().when(gameService).addToLibrary(gameId);

        mockMvc.perform(post("/user/add-to-library/{id}", gameId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/library"));

        verify(gameService, times(1)).addToLibrary(gameId);
    }

    @Test
    void testShoppingCartRemove() throws Exception {
        Long gameId = 1L;

        doNothing().when(shoppingCartService).removeItem(gameId);

        mockMvc.perform(post("/user/shopping-cart/remove/{id}", gameId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/shopping-cart"));

        verify(shoppingCartService, times(1)).removeItem(gameId);
    }

    @Test
    void testShoppingCartRemoveAll() throws Exception {
        doNothing().when(shoppingCartService).removeAll();

        mockMvc.perform(post("/user/shopping-cart/remove-all"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/shopping-cart"));

        verify(shoppingCartService, times(1)).removeAll();
    }

    @Test
    void testOrders() throws Exception {
        User mockUser = new User();
        mockUser.setId(1L);

        when(userHelperService.getUser()).thenReturn(mockUser);

        OrderResponseDTO mockOrder = new OrderResponseDTO();
        mockOrder.setTotalPrice(BigDecimal.valueOf(49.99));

        List<OrderResponseDTO> mockOrders = List.of(mockOrder);

        when(orderService.getOrdersByUser(mockUser.getId())).thenReturn(mockOrders);

        mockMvc.perform(get("/user/orders"))
                .andExpect(status().isOk())
                .andExpect(view().name("user-orders"))
                .andExpect(model().attribute("orders", mockOrders));

        verify(orderService, times(1)).getOrdersByUser(mockUser.getId());
    }

    @Test
    void testGetOrderDetails() throws Exception {
        long orderId = 1L;

        OrderResponseDTO mockOrder = new OrderResponseDTO();
        mockOrder.setTotalPrice(BigDecimal.valueOf(49.99));

        when(orderService.getOrderById(orderId)).thenReturn(mockOrder);

        mockMvc.perform(get("/user/order/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(view().name("order-details"))
                .andExpect(model().attribute("order", mockOrder));

        verify(orderService, times(1)).getOrderById(orderId);
    }
}