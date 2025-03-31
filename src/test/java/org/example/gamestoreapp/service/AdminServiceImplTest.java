package org.example.gamestoreapp.service;

import org.example.gamestoreapp.exception.FileUploadException;
import org.example.gamestoreapp.exception.GenreNotFoundException;
import org.example.gamestoreapp.model.dto.*;
import org.example.gamestoreapp.model.entity.Game;
import org.example.gamestoreapp.model.entity.Genre;
import org.example.gamestoreapp.model.entity.User;
import org.example.gamestoreapp.model.enums.UserRole;
import org.example.gamestoreapp.repository.GameRepository;
import org.example.gamestoreapp.repository.GenreRepository;
import org.example.gamestoreapp.repository.UserRepository;
import org.example.gamestoreapp.service.impl.AdminServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminServiceImplTest {
    @Mock
    private ModelMapper modelMapper;

    @Mock
    private GameRepository gameRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GenreRepository genreRepository;

    @Mock
    private AzureBlobStorageService azureBlobStorageService;

    @InjectMocks
    private AdminServiceImpl adminService;

    private User user;
    private Game game1, game2;
    private Genre genre;
    private GameDTO gameDTO1, gameDTO2;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setRole(UserRole.USER);
        user.setEnabled(false);

        game1 = new Game();
        game1.setId(1L);
        game1.setTitle("Title1");
        game1.setDescription("Description1");
        game1.setPublisher("Test Publisher");
        game1.setImageUrl("https://example.com/test-image.jpg");
        game1.setVideoUrl("https://example.com/test-video.mp4");
        game1.setReleaseDate(LocalDate.parse("2025-01-01"));
        game1.setPrice(BigDecimal.valueOf(49.99));

        game2 = new Game();
        game2.setId(2L);
        game2.setTitle("Title2");
        game2.setDescription("Description2");
        game2.setPublisher("Test Publisher");
        game2.setImageUrl("https://example.com/test-image.jpg");
        game2.setVideoUrl("https://example.com/test-video.mp4");
        game2.setReleaseDate(LocalDate.parse("2025-01-01"));
        game2.setPrice(BigDecimal.valueOf(49.99));

        gameDTO1 = new GameDTO();
        gameDTO1.setId(1L);
        gameDTO1.setTitle("Title1");

        gameDTO2 = new GameDTO();
        gameDTO2.setId(2L);
        gameDTO2.setTitle("Title2");

        genre = new Genre();
        genre.setId(1L);
        genre.setName("Action");
    }

    @Test
    void testAddGame_Success() throws IOException {
        AddGameBindingModel addGameBindingModel = new AddGameBindingModel();
        addGameBindingModel.setTitle("Test Title");
        addGameBindingModel.setDescription("Test Description");
        addGameBindingModel.setImageUrl("https://example.com/test-image.jpg");
        addGameBindingModel.setVideoUrl("https://example.com/test-video.mp4");
        addGameBindingModel.setPublisher("Test Publisher");
        addGameBindingModel.setReleaseDate(LocalDate.parse("2025-12-01"));
        addGameBindingModel.setPrice(BigDecimal.valueOf(99.99));
        addGameBindingModel.setGenre("Action");

        when(genreRepository.findByName("Action")).thenReturn(genre);

        when(azureBlobStorageService.uploadToAzureBlobStorage(anyString(), eq("images")))
                .thenReturn("https://mockstorage.blob.core.windows.net/images/mock-image.jpg");

        when(azureBlobStorageService.uploadToAzureBlobStorage(anyString(), eq("videos")))
                .thenReturn("https://mockstorage.blob.core.windows.net/videos/mock-video.mp4");

        adminService.addGame(addGameBindingModel);

        verify(gameRepository, times(1)).save(any(Game.class));
        verify(azureBlobStorageService, times(1)).uploadToAzureBlobStorage("https://example.com/test-image.jpg", "images");
        verify(azureBlobStorageService, times(1)).uploadToAzureBlobStorage("https://example.com/test-video.mp4", "videos");
    }

    @Test
    void testAddGame_WhenGenreNotFound_ShouldThrowException() {
        // Given
        AddGameBindingModel addGameBindingModel = new AddGameBindingModel();
        addGameBindingModel.setGenre("NonExistingGenre");

        when(genreRepository.findByName("NonExistingGenre")).thenReturn(null);

        // When / Then
        Exception exception = assertThrows(GenreNotFoundException.class, () -> adminService.addGame(addGameBindingModel));

        assertEquals("Genre 'NonExistingGenre' not found", exception.getMessage());
    }

    @Test
    void testAddGame_WhenFileUploadFails_ShouldThrowException() throws IOException {
        // Given
        AddGameBindingModel addGameBindingModel = new AddGameBindingModel();
        addGameBindingModel.setImageUrl("https://example.com/test-image.jpg");
        addGameBindingModel.setVideoUrl("https://example.com/test-video.mp4");
        addGameBindingModel.setGenre("Action");

        when(genreRepository.findByName("Action")).thenReturn(genre);

        // Simulate an upload failure
        when(azureBlobStorageService.uploadToAzureBlobStorage(anyString(), anyString()))
                .thenThrow(new FileUploadException("Failed to upload", new IOException()));

        // When / Then
        Exception exception = assertThrows(RuntimeException.class, () -> adminService.addGame(addGameBindingModel));

        assertTrue(exception.getMessage().contains("Game upload failed due to file storage error"));
    }

    @Test
    void testGetAllUsers_ShouldReturnUserDTOList() {
        User user1 = new User();
        user1.setId(1L);
        user1.setFirstName("First Name 1");
        user1.setLastName("Last Name 1");
        user1.setEmail("user1@email.com");
        user1.setRole(UserRole.USER);

        User user2 = new User();
        user2.setId(2L);
        user2.setFirstName("First Name 2");
        user2.setLastName("Last Name 2");
        user2.setEmail("user2@email.com");
        user2.setRole(UserRole.USER);

        List<User> mockUsers = List.of(user1, user2);

        UserDTO userDTO1 = new UserDTO();
        userDTO1.setId(1L);
        userDTO1.setFirstName("firstName");
        userDTO1.setLastName("lastName");
        userDTO1.setEmail("user1@email.com");
        userDTO1.setRole(UserRole.USER);
        UserDTO userDTO2 = new UserDTO();
        userDTO2.setId(2L);
        userDTO2.setFirstName("firstName");
        userDTO2.setLastName("lastName");
        userDTO2.setEmail("user2@email.com");
        userDTO2.setRole(UserRole.USER);

        List<UserDTO> expectedUserDTOList = List.of(userDTO1, userDTO2);

        when(userRepository.findAll()).thenReturn(mockUsers);

        when(modelMapper.map(user1, UserDTO.class)).thenReturn(userDTO1);
        when(modelMapper.map(user2, UserDTO.class)).thenReturn(userDTO2);

        List<UserDTO> actualUserDTOList = adminService.getAllUsers();

        assertThat(actualUserDTOList)
                .usingRecursiveComparison()
                .isEqualTo(expectedUserDTOList);

        verify(userRepository, times(1)).findAll();
        verify(modelMapper, times(2)).map(any(User.class), eq(UserDTO.class));
    }

    @Test
    void testPromote_UserExist() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        adminService.promote(1L);

        assertEquals(UserRole.ADMIN, user.getRole());

        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testPromote_UserNotExist() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        adminService.promote(2L);

        verify(userRepository, never()).save(any());
    }

    @Test
    void testDemote_UserExist() {
        user.setRole(UserRole.ADMIN);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        adminService.demote(1L);

        assertEquals(UserRole.USER, user.getRole());

        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testDemote_UserNotExist() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        adminService.demote(2L);

        verify(userRepository, never()).save(any());
    }

    @Test
    void testToggleUserState_UserExist() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        adminService.toggleUserState(1L);
        assertTrue(user.isEnabled());

        adminService.toggleUserState(1L);
        assertFalse(user.isEnabled());

        verify(userRepository, times(2)).save(user);
    }

    @Test
    void testToggleUserState_UserNotExist() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        adminService.toggleUserState(2L);

        verify(userRepository, never()).save(any());
    }

    @Test
    void testGetAllGames_ShouldReturnGameDTOList() {

        List<Game> mockGames = List.of(game1, game2);

        List<GameDTO> expectedGameDTOList = List.of(gameDTO1, gameDTO2);

        when(gameRepository.findAll()).thenReturn(mockGames);

        when(modelMapper.map(game1, GameDTO.class)).thenReturn(gameDTO1);
        when(modelMapper.map(game2, GameDTO.class)).thenReturn(gameDTO2);

        List<GameDTO> actualGameDTOList = adminService.getAllGames();

        assertThat(actualGameDTOList)
                .usingRecursiveComparison()
                .isEqualTo(expectedGameDTOList);

        verify(gameRepository, times(1)).findAll();
        verify(modelMapper, times(2)).map(any(Game.class), eq(GameDTO.class));
    }

    @Test
    void testDeleteGame() {
        doNothing().when(gameRepository).deleteById(1L);

        adminService.deleteGame(1L);

        verify(gameRepository, times(1)).deleteById(1L);
    }

    @Test
    void testGetGameById_GameExist() {
        UpdateGameBindingModel updateGameBindingModel = new UpdateGameBindingModel();
        updateGameBindingModel.setId(1L);
        updateGameBindingModel.setTitle("Game 1");
        updateGameBindingModel.setDescription("Game 1 description");

        when(gameRepository.findById(1L)).thenReturn(Optional.of(game1));
        when(modelMapper.map(game1, UpdateGameBindingModel.class)).thenReturn(updateGameBindingModel);

        UpdateGameBindingModel result = adminService.getGameById(1L);

        assertNotNull(result);
        assertEquals(updateGameBindingModel, result);

        verify(gameRepository, times(1)).findById(1L);
        verify(modelMapper, times(1)).map(any(Game.class), eq(UpdateGameBindingModel.class));
    }

    @Test
    void testGetGameById_GameNotExist() {
        when(gameRepository.findById(3L)).thenReturn(Optional.empty());

        UpdateGameBindingModel result = adminService.getGameById(3L);

        assertNull(result);

        verify(gameRepository, times(1)).findById(3L);
    }

    @Test
    void testEditGame_SuccessfulUpdate() throws IOException {

        UpdateGameBindingModel updateGameBindingModel = new UpdateGameBindingModel();
        updateGameBindingModel.setId(1L);
        updateGameBindingModel.setTitle("Updated Title");
        updateGameBindingModel.setDescription("Updated Description");
        updateGameBindingModel.setImageUrl("https://example.com/updated-image.jpg");
        updateGameBindingModel.setVideoUrl("https://example.com/updated-video.mp4");
        updateGameBindingModel.setPublisher("Updated Publisher");
        updateGameBindingModel.setReleaseDate(LocalDate.parse("2024-12-01"));
        updateGameBindingModel.setPrice(BigDecimal.valueOf(99.99));
        updateGameBindingModel.setGenre("Action");

        when(gameRepository.findById(1L)).thenReturn(Optional.of(game1));
        when(genreRepository.findByName("Action")).thenReturn(genre);
        when(azureBlobStorageService.uploadToAzureBlobStorage(anyString(), eq("images")))
                .thenReturn("https://mockstorage.blob.core.windows.net/images/mock-image.jpg");
        when(azureBlobStorageService.uploadToAzureBlobStorage(anyString(), eq("videos")))
                .thenReturn("https://mockstorage.blob.core.windows.net/videos/mock-video.mp4");

        adminService.editGame(updateGameBindingModel);

        verify(gameRepository, times(1)).findById(1L);
        verify(gameRepository, times(1)).save(any(Game.class));
        verify(azureBlobStorageService, times(1)).uploadToAzureBlobStorage(anyString(), eq("images"));
        verify(azureBlobStorageService, times(1)).uploadToAzureBlobStorage(anyString(), eq("videos"));

        assertEquals("Updated Title", game1.getTitle());
        assertEquals("Updated Description", game1.getDescription());
        assertEquals("https://mockstorage.blob.core.windows.net/images/mock-image.jpg", game1.getImageUrl());
        assertEquals("https://mockstorage.blob.core.windows.net/videos/mock-video.mp4", game1.getVideoUrl());
        assertEquals("Updated Publisher", game1.getPublisher());
        assertEquals(LocalDate.parse("2024-12-01"), game1.getReleaseDate());
        assertEquals(BigDecimal.valueOf(99.99), game1.getPrice());
        assertEquals("Action", game1.getGenre().getName());
    }

    @Test
    void testEditGame_GameNotFound() throws IOException {
        when(gameRepository.findById(99L)).thenReturn(Optional.empty());

        UpdateGameBindingModel updateGameBindingModel = new UpdateGameBindingModel();
        updateGameBindingModel.setId(99L);

        adminService.editGame(updateGameBindingModel);
        verify(gameRepository, times(1)).findById(99L);
        verify(gameRepository, never()).save(any(Game.class));
        verify(azureBlobStorageService, never()).uploadToAzureBlobStorage(anyString(), anyString());
    }

    @Test
    void testEditGame_GenreNotFound() {
        UpdateGameBindingModel updateGameBindingModel = new UpdateGameBindingModel();
        updateGameBindingModel.setId(1L);
        updateGameBindingModel.setGenre("Unknown Genre");

        when(gameRepository.findById(1L)).thenReturn(Optional.of(game1));
        when(genreRepository.findByName("Unknown Genre")).thenReturn(null);

        assertThrows(GenreNotFoundException.class, () -> adminService.editGame(updateGameBindingModel));
        verify(genreRepository, times(1)).findByName("Unknown Genre");
        verify(gameRepository, times(1)).findById(1L);
        verify(gameRepository, never()).save(any(Game.class));
    }

    @Test
    void testEditGame_NoUploadIfUrlsUnchanged() throws IOException {
        game1.setImageUrl("https://example.com/existing-image.jpg");
        game1.setVideoUrl("https://example.com/existing-video.mp4");

        UpdateGameBindingModel updateGameBindingModel = new UpdateGameBindingModel();
        updateGameBindingModel.setId(1L);
        updateGameBindingModel.setImageUrl("https://example.com/existing-image.jpg");
        updateGameBindingModel.setVideoUrl("https://example.com/existing-video.mp4");
        updateGameBindingModel.setGenre("Action");

        when(gameRepository.findById(1L)).thenReturn(Optional.of(game1));
        when(genreRepository.findByName("Action")).thenReturn(genre);

        adminService.editGame(updateGameBindingModel);

        verify(azureBlobStorageService, never()).uploadToAzureBlobStorage(anyString(), anyString());
        verify(gameRepository, times(1)).save(game1);
    }

    @Test
    void testGetAllGenres_ShouldReturnGenreDTOList() {
        Genre genre1 = new Genre();
        genre1.setId(1L);
        genre1.setName("MMO");
        genre1.setDescription("MMO Description");

        Genre genre2 = new Genre();
        genre2.setId(2L);
        genre2.setName("RPG");
        genre2.setDescription("RPG Description");

        GenreDTO genreDTO1 = new GenreDTO();
        genreDTO1.setId(1L);
        genreDTO1.setName("MMO");
        genreDTO1.setDescription("MMO Description");

        GenreDTO genreDTO2 = new GenreDTO();
        genreDTO2.setId(2L);
        genreDTO2.setName("RPG");
        genreDTO2.setDescription("RPG Description");

        List<Genre> mockGenres = List.of(genre1, genre2);

        List<GenreDTO> expectedGenreDTOList = List.of(genreDTO1, genreDTO2);

        when(genreRepository.findAll()).thenReturn(mockGenres);

        when(modelMapper.map(genre1, GenreDTO.class)).thenReturn(genreDTO1);
        when(modelMapper.map(genre2, GenreDTO.class)).thenReturn(genreDTO2);

        List<GenreDTO> actualGenreDTOList = adminService.getAllGenres();

        assertEquals(expectedGenreDTOList, actualGenreDTOList);

        verify(genreRepository, times(1)).findAll();
        verify(modelMapper, times(2)).map(any(Genre.class), eq(GenreDTO.class));
    }

    @Test
    void testAddGenre_Success() {
        AddGenreBindingModel addGenreBindingModel = new AddGenreBindingModel();
        addGenreBindingModel.setName("New Genre");

        adminService.addGenre(addGenreBindingModel);

        verify(genreRepository, times(1)).save(any(Genre.class));
    }

    @Test
    void testGetGenreById_GenreExist() {
        UpdateGenreBindingModel updateGenreBindingModel = new UpdateGenreBindingModel();
        updateGenreBindingModel.setId(1L);

        when(genreRepository.findById(1L)).thenReturn(Optional.ofNullable(genre));
        when(modelMapper.map(genre, UpdateGenreBindingModel.class)).thenReturn(updateGenreBindingModel);

        UpdateGenreBindingModel result = adminService.getGenreById(1L);

        assertNotNull(result);
        assertEquals(updateGenreBindingModel, result);

        verify(genreRepository, times(1)).findById(1L);
        verify(modelMapper, times(1)).map(any(Genre.class), eq(UpdateGenreBindingModel.class));
    }

    @Test
    void testGetGenreById_GenreNotExist() {
        when(genreRepository.findById(99L)).thenReturn(Optional.empty());

        assertNull(adminService.getGenreById(99L));

        verify(genreRepository, times(1)).findById(99L);
    }

    @Test
    void testEditGenre_SuccessfulUpdate() {
        UpdateGenreBindingModel updateGenreBindingModel = new UpdateGenreBindingModel();
        updateGenreBindingModel.setId(1L);
        updateGenreBindingModel.setName("New name");
        updateGenreBindingModel.setDescription("New description");

        when(genreRepository.findById(1L)).thenReturn(Optional.of(genre));

        adminService.editGenre(updateGenreBindingModel);

        verify(genreRepository, times(1)).findById(1L);
        verify(genreRepository, times(1)).save(any(Genre.class));

        assertEquals("New name", genre.getName());
        assertEquals("New description", genre.getDescription());
    }

    @Test
    void testEditGenre_GenreNotFound() {
        when(genreRepository.findById(99L)).thenReturn(Optional.empty());

        UpdateGenreBindingModel updateGenreBindingModel = new UpdateGenreBindingModel();
        updateGenreBindingModel.setId(99L);

        adminService.editGenre(updateGenreBindingModel);

        verify(genreRepository, times(1)).findById(99L);
        verify(genreRepository, never()).save(any(Genre.class));
    }

    @Test
    void testDeleteGenre() {
        adminService.deleteGenre(1L);

        verify(genreRepository, times(1)).deleteById(1L);
    }
}
