package org.example.gamestoreapp.controller;

import org.example.gamestoreapp.model.dto.*;
import org.example.gamestoreapp.model.enums.UserRole;
import org.example.gamestoreapp.service.AdminService;
import org.example.gamestoreapp.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class AdminControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AdminService adminService;

    @Mock
    private OrderService orderService;

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

    @Test
    void testPromoteUser() throws Exception {
        long userId = 1L;

        mockMvc.perform(post("/admin/user/promote/{id}", userId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));

        verify(adminService, times(1)).promote(1L);
    }

    @Test
    void testDemoteUser() throws Exception {
        long userId = 1L;

        mockMvc.perform(post("/admin/user/demote/{id}", userId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));

        verify(adminService, times(1)).demote(1L);
    }

    @Test
    void testChangeUserStatus() throws Exception {
        long userId = 1L;

        mockMvc.perform(post("/admin/user/change/{id}", userId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));

        verify(adminService, times(1)).toggleUserState(userId);
    }

    @Test
    void testAddGameView() throws Exception {
        GenreDTO genre1 = new GenreDTO();
        genre1.setName("Genre 1");

        GenreDTO genre2 = new GenreDTO();
        genre2.setName("Genre 2");

        List<GenreDTO> mockGenres = List.of(genre1, genre2);

        when(adminService.getAllGenres()).thenReturn(mockGenres);

        mockMvc.perform(get("/admin/add-game"))
                .andExpect(status().isOk())
                .andExpect(view().name("add_game"))
                .andExpect(model().attributeExists("genres"))
                .andExpect(model().attribute("genres", mockGenres))
                .andExpect(model().attributeExists("addGameBindingModel"));

        verify(adminService, times(1)).getAllGenres();
    }

    @Test
    void testAddGameViewWithExistingModel() throws Exception {
        GenreDTO genre1 = new GenreDTO();
        genre1.setName("Genre 1");

        GenreDTO genre2 = new GenreDTO();
        genre2.setName("Genre 2");

        List<GenreDTO> mockGenres = List.of(genre1, genre2);

        // Simulate that the model already has the AddGameBindingModel
        AddGameBindingModel existingModel = new AddGameBindingModel();
        existingModel.setTitle("Test Title");

        when(adminService.getAllGenres()).thenReturn(mockGenres);

        mockMvc.perform(get("/admin/add-game")
                        .flashAttr("addGameBindingModel", existingModel)) // Simulating the existing model
                .andExpect(status().isOk())
                .andExpect(view().name("add_game"))
                .andExpect(model().attributeExists("genres"))
                .andExpect(model().attribute("genres", mockGenres))
                .andExpect(model().attribute("addGameBindingModel", existingModel)) // Checking that the model is passed
                .andExpect(model().attributeExists("addGameBindingModel"));

        verify(adminService, times(1)).getAllGenres();
    }

    @Test
    void testAddGameWithBindingErrors() throws Exception {
        // Create a mock AddGameBindingModel with invalid data
        AddGameBindingModel invalidModel = new AddGameBindingModel();
        // Set invalid values that will trigger binding validation errors
        invalidModel.setTitle("");  // Assuming the title is required and empty will trigger validation error

        // Create a mock BindingResult with errors
        BindingResult bindingResult = new BeanPropertyBindingResult(invalidModel, "addGameBindingModel");
        bindingResult.rejectValue("title", "NotEmpty.addGameBindingModel.title");  // Example error message for the title field

        // Perform the POST request and simulate the BindingResult
        mockMvc.perform(post("/admin/add-game"))
                .andExpect(status().is3xxRedirection())  // Expecting a redirection
                .andExpect(redirectedUrl("/admin/add-game")) // The controller should redirect back to the same page
                .andExpect(flash().attributeExists("addGameBindingModel"))
                .andExpect(flash().attributeExists("org.springframework.validation.BindingResult.addGameBindingModel"));
    }

    @Test
    void testAddGame_SuccessfulSubmission() throws Exception {

        mockMvc.perform(post("/admin/add-game")
                        .param("title", "Test Title")
                        .param("description", "Test Description")
                        .param("imageUrl", "https://example.com/test.jpg")
                        .param("videoUrl", "")
                        .param("publisher", "publisher")
                        .param("genre", "Action")
                        .param("price", "49.99")
                        .param("releaseDate", "2024-11-22"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/games"));

        verify(adminService, times(1)).addGame(any(AddGameBindingModel.class));
    }

    @Test
    void testDeleteGame() throws Exception {
        mockMvc.perform(post("/admin/game/delete/{id}", 1L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/games"));

        verify(adminService, times(1)).deleteGame(1L);
    }

    @Test
    void testEditGame() throws Exception {
        GenreDTO genre1 = new GenreDTO();
        genre1.setName("Genre 1");

        GenreDTO genre2 = new GenreDTO();
        genre2.setName("Genre 2");

        List<GenreDTO> mockGenres = List.of(genre1, genre2);

        UpdateGameBindingModel mockModel = new UpdateGameBindingModel();
        mockModel.setId(1L);
        mockModel.setTitle("Test Title");
        mockModel.setDescription("Test Description");
        mockModel.setPrice(BigDecimal.valueOf(49.99));

        when(adminService.getAllGenres()).thenReturn(mockGenres);
        when(adminService.getGameById(1L)).thenReturn(mockModel);

        mockMvc.perform(get("/admin/game/edit/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("edit-game"))
                .andExpect(model().attribute("genres", mockGenres))
                .andExpect(model().attribute("bindingModel", mockModel));

        verify(adminService, times(1)).getAllGenres();
        verify(adminService, times(1)).getGameById(1L);
    }

    @Test
    void testEditGameWithBindingErrors() throws Exception {
        long gameId = 1L;

        mockMvc.perform(post("/admin/game/edit")
                        .param("id", Long.toString(gameId))
                        .param("title", "")) // Assuming the title is required and empty will trigger validation error
                .andExpect(status().is3xxRedirection())  // Expecting a redirection
                .andExpect(redirectedUrl("/admin/game/edit/" + gameId)) // The controller should redirect back to the same page
                .andExpect(flash().attributeExists("updateGameBindingModel"))
                .andExpect(flash().attributeExists("org.springframework.validation.BindingResult.updateGameBindingModel"));
    }

    @Test
    void testEditGame_SuccessfulSubmission() throws Exception {

        mockMvc.perform(post("/admin/game/edit")
                        .param("title", "Test Title")
                        .param("description", "Test Description")
                        .param("imageUrl", "https://example.com/test.jpg")
                        .param("videoUrl", "")
                        .param("publisher", "publisher")
                        .param("genre", "Action")
                        .param("price", "49.99")
                        .param("releaseDate", "2024-11-22"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/games"));

        verify(adminService, times(1)).editGame(any(UpdateGameBindingModel.class));
    }

    @Test
    void testGenresEndpoint() throws Exception {
        mockMvc.perform(get("/admin/genres"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("genres"))
                .andExpect(view().name("manage-genres"));

        verify(adminService, times(1)).getAllGenres();
    }

    @Test
    void testAddGenre() throws Exception {
        mockMvc.perform(get("/admin/add-genre"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("addGenreBindingModel"))
                .andExpect(view().name("add_genre"));
    }

    @Test
    void testAddGenreWithExistingModel() throws Exception {
        AddGenreBindingModel existingModel = new AddGenreBindingModel();
        existingModel.setName("Test Genre");

        mockMvc.perform(get("/admin/add-genre")
                .flashAttr("addGenreBindingModel", existingModel))
                .andExpect(status().isOk())
                .andExpect(model().attribute("addGenreBindingModel", existingModel))
                .andExpect(view().name("add_genre"));
    }

    @Test
    void testAddGenreWithBindingErrors() throws Exception {
        AddGenreBindingModel invalidModel = new AddGenreBindingModel();
        invalidModel.setName("");  // Assuming the name is required and empty will trigger validation error

        // Create a mock BindingResult with errors
        BindingResult bindingResult = new BeanPropertyBindingResult(invalidModel, "addGenreBindingModel");
        bindingResult.rejectValue("name", "NotEmpty.addGenreBindingModel.name");  // Example error message for the title field

        mockMvc.perform(post("/admin/add-genre")
                .flashAttr("addGenreBindingModel", invalidModel))
                .andExpect(status().is3xxRedirection())  // Expecting a redirection
                .andExpect(redirectedUrl("/admin/add-genre")) // The controller should redirect back to the same page
                .andExpect(flash().attributeExists("addGenreBindingModel"))
                .andExpect(flash().attributeExists("org.springframework.validation.BindingResult.addGenreBindingModel"));
    }

    @Test
    void testAddGenre_SuccessfulSubmission() throws Exception {

        mockMvc.perform(post("/admin/add-genre")
                        .param("name", "Test Genre")
                        .param("description", "Test Genre Description"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/genres"));

        verify(adminService, times(1)).addGenre(any(AddGenreBindingModel.class));
    }

    @Test
    void testEditGenreWithExistingModel() throws Exception {
        long genreId = 1L;

        UpdateGenreBindingModel existingModel = new UpdateGenreBindingModel();
        existingModel.setName("Test Genre");
        existingModel.setDescription("Test Genre Description");
        existingModel.setId(genreId);

        mockMvc.perform(get("/admin/genre/edit/{id}", genreId)
                        .flashAttr("updateGenreBindingModel", existingModel))
                .andExpect(status().isOk())
                .andExpect(model().attribute("updateGenreBindingModel", existingModel))
                .andExpect(view().name("edit-genre"));
    }

    @Test
    void testEditGenre() throws Exception {
        long genreId = 1L;

        UpdateGenreBindingModel mockModel = new UpdateGenreBindingModel();
        mockModel.setName("Test Genre");

        when(adminService.getGenreById(genreId)).thenReturn(mockModel);

        mockMvc.perform(get("/admin/genre/edit/{id}", genreId))
                .andExpect(status().isOk())
                .andExpect(model().attribute("updateGenreBindingModel", mockModel))
                .andExpect(view().name("edit-genre"));

        verify(adminService, times(1)).getGenreById(genreId);
    }

    @Test
    void testEditGenreWithBindingErrors() throws Exception {
        long genreId = 1L;

        UpdateGenreBindingModel invalidModel = new UpdateGenreBindingModel();
        invalidModel.setId(genreId);
        invalidModel.setName("");  // Assuming the name is required and empty will trigger validation error

        // Create a mock BindingResult with errors
        BindingResult bindingResult = new BeanPropertyBindingResult(invalidModel, "updateGenreBindingModel");
        bindingResult.rejectValue("name", "NotEmpty.updateGenreBindingModel.name");

        mockMvc.perform(post("/admin/genre/edit")
                        .flashAttr("updateGenreBindingModel", invalidModel))
                .andExpect(status().is3xxRedirection())  // Expecting a redirection
                .andExpect(redirectedUrl("/admin/genre/edit/" + genreId)) // The controller should redirect back to the same page
                .andExpect(flash().attributeExists("updateGenreBindingModel"))
                .andExpect(flash().attributeExists("org.springframework.validation.BindingResult.updateGenreBindingModel"));
    }

    @Test
    void testEditGenre_SuccessfulSubmission() throws Exception {

        mockMvc.perform(post("/admin/genre/edit")
                        .param("name", "Test Name")
                        .param("description", "Test Description"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/genres"));

        verify(adminService, times(1)).editGenre(any(UpdateGenreBindingModel.class));
    }

    @Test
    void testDeleteGenre() throws Exception {
        long genreId = 1L;

        mockMvc.perform(delete("/admin/genre/delete/{id}", genreId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/genres"));

        verify(adminService, times(1)).deleteGenre(genreId);
    }

    @Test
    void testGetOrders() throws Exception {
        OrderResponseDTO mockResponse = new OrderResponseDTO();
        mockResponse.setId(1L);

        List<OrderResponseDTO> mockOrders = List.of(mockResponse);

        when(orderService.getAllOrders()).thenReturn(mockOrders);

        mockMvc.perform(get("/admin/orders"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("orders"))
                .andExpect(view().name("order-list"));
    }

    @Test
    void testGetOrderDetails() throws Exception {
        long orderId = 1L;

        OrderResponseDTO mockResponse = new OrderResponseDTO();
        mockResponse.setId(orderId);

        when(orderService.getOrderById(orderId)).thenReturn(mockResponse);

        mockMvc.perform(get("/admin/order/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(model().attribute("order", mockResponse))
                .andExpect(view().name("order-details"));

        verify(orderService, times(1)).getOrderById(orderId);
    }
}
