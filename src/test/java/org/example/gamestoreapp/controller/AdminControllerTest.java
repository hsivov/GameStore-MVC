package org.example.gamestoreapp.controller;

import org.example.gamestoreapp.model.dto.GameDTO;
import org.example.gamestoreapp.model.dto.UserDTO;
import org.example.gamestoreapp.model.enums.UserRole;
import org.example.gamestoreapp.service.AdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class AdminControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AdminService adminService;

    @InjectMocks
    private AdminController adminController;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminController).build();
    }

    @Test
    public void testGamesEndpoint() throws Exception {
        GameDTO game1 = new GameDTO();
        game1.setId(1L);
        game1.setTitle("Game 1");
        game1.setDescription("Game 1 Description");
        game1.setImageUrl("imageUrl");
        game1.setVideoUrl("videoUrl");
        game1.setPublisher("publisher");
        game1.setReleaseDate("releaseDate");
        game1.setGenre("genre");
        game1.setPrice(BigDecimal.valueOf(10.00));

        GameDTO game2 = new GameDTO();
        game2.setId(2L);
        game2.setTitle("Game 2");
        game2.setDescription("Game 2 Description");
        game2.setImageUrl("imageUrl");
        game2.setVideoUrl("videoUrl");
        game2.setPublisher("publisher");
        game2.setReleaseDate("releaseDate");
        game2.setGenre("genre");
        game2.setPrice(BigDecimal.valueOf(20.00));

        List<GameDTO> mockGames = Arrays.asList(game1, game2);

        when(adminService.getAllGames()).thenReturn(mockGames);

        mockMvc.perform(get("/admin/games"))
                .andExpect(status().isOk())
                .andExpect(view().name("manage-games"))
                .andExpect(model().attributeExists("games"))
                .andExpect(model().attribute("games", mockGames));

        verify(adminService, times(1)).getAllGames();
    }

    @Test
    public void testUsersEndpoint() throws Exception {
        UserDTO user1 = new UserDTO();
        user1.setId(1L);
        user1.setFirstName("First Name");
        user1.setLastName("Last Name");
        user1.setEmail("email@email.com");
        user1.setRole(UserRole.USER);

        UserDTO user2 = new UserDTO();
        user2.setId(2L);
        user2.setFirstName("First Name");
        user2.setLastName("Last Name");
        user2.setEmail("email@email.com");
        user2.setRole(UserRole.USER);

        List<UserDTO> mockUsers = Arrays.asList(user1, user2);

        when(adminService.getAllUsers()).thenReturn(mockUsers);

        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(view().name("manage-users"))
                .andExpect(model().attributeExists("users"))
                .andExpect(model().attribute("users", mockUsers));

        verify(adminService, times(1)).getAllUsers();
    }
}
