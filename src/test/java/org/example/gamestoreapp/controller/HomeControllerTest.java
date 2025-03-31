package org.example.gamestoreapp.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.gamestoreapp.model.dto.GameDTO;
import org.example.gamestoreapp.service.GameService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class HomeControllerTest {

    private MockMvc mockMvc;

    @Mock
    private GameService gameService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private Model model;

    @InjectMocks
    private HomeController homeController;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(homeController)
                .setViewResolvers(new InternalResourceViewResolver("templates", "html"))
                .build();
    }

    @Test
    public void testHomeEndpoint() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("currentUrl"));
    }

    @Test
    public void testHomeMethodDirectly() {
        when(request.getRequestURI()).thenReturn("/");

        String viewName = homeController.home(request, model);

        assertEquals("index", viewName);
        verify(model).addAttribute("currentUrl", request.getRequestURI());
    }

    @Test
    public void testLibraryEndpoint() throws Exception {
        GameDTO ownedGame1 = new GameDTO();
        ownedGame1.setId(1L);
        ownedGame1.setTitle("Game 1");
        ownedGame1.setDescription("Game 1 Description");
        ownedGame1.setImageUrl("imageUrl");
        ownedGame1.setVideoUrl("videoUrl");
        ownedGame1.setPublisher("publisher");
        ownedGame1.setReleaseDate("releaseDate");
        ownedGame1.setGenre("genre");
        ownedGame1.setPrice(BigDecimal.valueOf(10.00));

        GameDTO ownedGame2 = new GameDTO();
        ownedGame2.setId(2L);
        ownedGame2.setTitle("Game 2");
        ownedGame2.setDescription("Game 2 Description");
        ownedGame2.setImageUrl("imageUrl");
        ownedGame2.setVideoUrl("videoUrl");
        ownedGame2.setPublisher("publisher");
        ownedGame2.setReleaseDate("releaseDate");
        ownedGame2.setGenre("genre");
        ownedGame2.setPrice(BigDecimal.valueOf(10.00));

        List<GameDTO> ownedGames = Arrays.asList(ownedGame1, ownedGame2);

        when(gameService.getOwnedGames()).thenReturn(ownedGames);
        mockMvc.perform(get("/library"))
                .andExpect(status().isOk())
                .andExpect(view().name("library"))
                .andExpect(model().attributeExists("ownedGames"))
                .andExpect(model().attribute("ownedGames", ownedGames));

        verify(gameService, times(1)).getOwnedGames();
    }

    @Test
    public void testContactsEndpoint() throws Exception {
        mockMvc.perform(get("/contacts"))
                .andExpect(status().isOk())
                .andExpect(view().name("contacts"));
    }
}
