package org.example.gamestoreapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.gamestoreapp.config.TestSecurityConfig;
import org.example.gamestoreapp.interceptor.HMACInterceptor;
import org.example.gamestoreapp.model.dto.GameDTO;
import org.example.gamestoreapp.service.GameService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GameRestController.class)
@Import(TestSecurityConfig.class)
class GameRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GameService gameService;

    @MockBean
    private HMACInterceptor hmacInterceptor;

    private GameDTO mockGame;
    private Long gameId;

    @BeforeEach
    void setUp() {
        // Arrange: Mock game data
        gameId = 1L;
        mockGame = new GameDTO();
        mockGame.setId(gameId);
        mockGame.setTitle("Elden Ring");
        mockGame.setGenre("RPG");
    }

    @Test
    void testGetGameById_WhenGameExists() throws Exception {

        when(gameService.getGameById(gameId)).thenReturn(Optional.of(mockGame));

        // Mock interceptor to always return true (i.e., allow request)
        when(hmacInterceptor.preHandle(any(), any(), any())).thenReturn(true);

        // Act & Assert: Perform GET request and check response
        mockMvc.perform(get("/api/games/{gameId}", gameId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(gameId))
                .andExpect(jsonPath("$.title").value("Elden Ring"))
                .andExpect(jsonPath("$.genre").value("RPG"));
    }

    @Test
    void testGetGameById_WhenGameDoesNotExist() throws Exception {
        // Arrange: No game found for given ID
        Long gameId = 99L;
        when(gameService.getGameById(gameId)).thenReturn(Optional.empty());

        when(hmacInterceptor.preHandle(any(), any(), any())).thenReturn(true);

        // Act & Assert
        mockMvc.perform(get("/api/games/{gameId}", gameId))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetGameById_WhenGameIdIsInvalid() throws Exception {
        when(hmacInterceptor.preHandle(any(), any(), any())).thenReturn(true);

        // Act & Assert: Sending an invalid (non-numeric) gameId
        mockMvc.perform(get("/api/games/{gameId}", "invalidId"))
                .andExpect(status().isBadRequest()); // Spring will handle this automatically
    }

    @Test
    void testGetGamesByIds() throws Exception {
        Set<Long> gameIds = Set.of(gameId);
        List<GameDTO> mockGames = List.of(mockGame);

        when(gameService.getGamesByIds(gameIds)).thenReturn(mockGames);
        when(hmacInterceptor.preHandle(any(), any(), any())).thenReturn(true);

        mockMvc.perform(post("/api/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(gameIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(gameId))
                .andExpect(jsonPath("$[0].title").value("Elden Ring"))
                .andExpect(jsonPath("$[0].genre").value("RPG"));
    }

    @Test
    void testGetGamesByIds_WhenGameListIsEmpty() throws Exception {
        Set<Long> gameIds = Set.of(gameId);
        List<GameDTO> emptyList = new ArrayList<>();

        when(gameService.getGamesByIds(gameIds)).thenReturn(emptyList);
        when(hmacInterceptor.preHandle(any(), any(), any())).thenReturn(true);

        mockMvc.perform(post("/api/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(gameIds)))
                .andExpect(status().isNotFound());
    }
}