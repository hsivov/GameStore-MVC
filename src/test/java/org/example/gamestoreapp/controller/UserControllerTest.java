package org.example.gamestoreapp.controller;

import org.example.gamestoreapp.config.TestConfig;
import org.example.gamestoreapp.config.TestSecurityConfig;
import org.example.gamestoreapp.model.view.UserProfileViewModel;
import org.example.gamestoreapp.service.*;
import org.example.gamestoreapp.service.session.UserHelperService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import({TestSecurityConfig.class, UserHelperService.class})
class UserControllerTest {
    @MockBean
    private UserService userService;

    @MockBean
    private GameService gameService;

    @MockBean
    private ShoppingCartService shoppingCartService;

    @MockBean
    private OrderService orderService;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserHelperService userHelperService;

    @MockBean
    private AzureBlobStorageService azureBlobStorageService;

    @Autowired
    private MockMvc mockMvc;

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Test
    void testProfileEndpoint() throws Exception {
        UserProfileViewModel mockProfileView = new UserProfileViewModel();
        mockProfileView.setUsername("username");

        when(userService.getProfileView()).thenReturn(mockProfileView);
        when(userHelperService.isAuthenticated()).thenReturn(true);

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

        when(azureBlobStorageService.uploadToAzureBlobStorage(file, "profile-images")).thenReturn(mockUrl);

        mockMvc.perform(multipart("/user/profile/upload-image")
                .file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.profileImageUrl").value(mockUrl));

        verify(azureBlobStorageService, times(1)).uploadToAzureBlobStorage(eq(file), eq("profile-images"));
    }

    @Test
    void testUploadImage_Failure() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "profileImage", "test.jpg", "image/jpeg", "test image content".getBytes()
        );

        String mockUrl = "https://azure.blob.storage/profile-images/test.jpg";

        when(azureBlobStorageService.uploadToAzureBlobStorage(file, "profile-images"))
                .thenThrow(new IOException("Upload failed"));

        mockMvc.perform(multipart("/user/profile/upload-image")
                        .file(file))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Error uploading file")));

        verify(azureBlobStorageService, times(1)).uploadToAzureBlobStorage(eq(file), eq("profile-images"));
    }
}