package org.example.gamestoreapp.controller;

import org.example.gamestoreapp.config.TestConfig;
import org.example.gamestoreapp.model.dto.CommentDTO;
import org.example.gamestoreapp.model.dto.GameDTO;
import org.example.gamestoreapp.model.dto.PostCommentDTO;
import org.example.gamestoreapp.service.CommentService;
import org.example.gamestoreapp.service.GameService;
import org.example.gamestoreapp.service.LibraryService;
import org.example.gamestoreapp.service.ShoppingCartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.hamcrest.Matchers.hasEntry;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class StoreControllerTest {

    @Mock
    private GameService gameService;

    @Mock
    private ShoppingCartService shoppingCartService;

    @Mock
    private LibraryService libraryService;

    @Mock
    private CommentService commentService;

    @InjectMocks
    private StoreController storeController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(storeController)
                .setViewResolvers(new TestConfig().viewResolver()).build();
    }

    @Test
    void testStoreEndpoint() throws Exception {
        Long gameId = 1L;
        GameDTO mockGameDTO = new GameDTO();
        mockGameDTO.setId(gameId);
        mockGameDTO.setTitle("Test Game");

        List<GameDTO> mockGames = List.of(mockGameDTO);

        when(gameService.getAll()).thenReturn(mockGames);
        when(libraryService.isGameInLibrary(gameId)).thenReturn(true);
        when(shoppingCartService.isGameInCart(gameId)).thenReturn(false);

        mockMvc.perform(get("/store"))
                .andExpect(status().isOk())
                .andExpect(view().name("store"))
                .andExpect(model().attribute("games", mockGames))
                .andExpect(model().attribute("gamesInLibrary", hasEntry(gameId, true)))
                .andExpect(model().attribute("gamesInShoppingCart", hasEntry(gameId, false)));

        verify(gameService, times(1)).getAll();
        verify(libraryService, times(1)).isGameInLibrary(gameId);
        verify(shoppingCartService, times(1)).isGameInCart(gameId);
    }

    @Test
    void testGetGameDetailsById_GameExists() throws Exception {
        Long gameId = 1L;
        GameDTO mockGameDTO = new GameDTO();
        mockGameDTO.setId(gameId);
        mockGameDTO.setTitle("Test Game");

        CommentDTO mockCommentDTO = new CommentDTO();
        mockCommentDTO.setContent("Test Comment");

        List<CommentDTO> mockComments = List.of(mockCommentDTO);

        when(gameService.getGameById(gameId)).thenReturn(Optional.of(mockGameDTO));
        when(libraryService.isGameInLibrary(gameId)).thenReturn(true);
        when(shoppingCartService.isGameInCart(gameId)).thenReturn(true);
        when(commentService.getCommentsByGame(gameId)).thenReturn(mockComments);

        mockMvc.perform(get("/store/game-details/{id}", gameId))
                .andExpect(status().isOk())
                .andExpect(view().name("game-details"))
                .andExpect(model().attribute("game", mockGameDTO))
                .andExpect(model().attribute("isInLibrary", true))
                .andExpect(model().attribute("isInCart", true))
                .andExpect(model().attribute("comments", mockComments));

        verify(gameService, times(1)).getGameById(gameId);
        verify(libraryService, times(1)).isGameInLibrary(gameId);
        verify(shoppingCartService, times(1)).isGameInCart(gameId);
        verify(commentService, times(1)).getCommentsByGame(gameId);
    }

    @Test
    void testGetGameDetailsById_WhenGameWasNotFound() throws Exception {
        Long gameId = 99L;

        when(gameService.getGameById(gameId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/store/game-details/{id}", gameId))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertInstanceOf(ResponseStatusException.class, result.getResolvedException()))
                .andExpect(result -> assertEquals("404 NOT_FOUND \"Game with ID " + gameId + " not found\"",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));

        verify(gameService, times(1)).getGameById(gameId);
    }

    @Test
    void testAddGameToShoppingCart() throws Exception {
        Long gameId = 1L;

        mockMvc.perform(post("/game-details/add-to-cart/{id}", gameId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/store/game-details/" + gameId));

        verify(shoppingCartService, times(1)).addToCart(gameId);
    }

    @Test
    void testPostComment_WithBindingErrors() throws Exception {
        Long gameId = 1L;

        PostCommentDTO invalidCommentDTO = new PostCommentDTO();
        invalidCommentDTO.setComment("");

        mockMvc.perform(post("/game-details/post-comment/{id}", gameId)
                        .flashAttr("postCommentDTO", invalidCommentDTO))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/store/game-details/" + gameId));

        verify(commentService, never()).postComment(invalidCommentDTO, gameId);
    }

    @Test
    void testPostComment_SubmitSuccess() throws Exception {
        Long gameId = 1L;

        PostCommentDTO validCommentDTO = new PostCommentDTO();
        validCommentDTO.setComment("Test Comment");

        mockMvc.perform(post("/game-details/post-comment/{id}", gameId)
                        .flashAttr("postCommentDTO", validCommentDTO))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/store/game-details/" + gameId));

        verify(commentService, times(1)).postComment(validCommentDTO, gameId);
    }
}