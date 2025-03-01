package org.example.gamestoreapp.service;

import org.example.gamestoreapp.exception.FileUploadException;
import org.example.gamestoreapp.exception.GenreNotFoundException;
import org.example.gamestoreapp.model.dto.AddGameBindingModel;
import org.example.gamestoreapp.model.dto.GameDTO;
import org.example.gamestoreapp.model.dto.UserDTO;
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
public class AdminServiceTest {
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

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setRole(UserRole.USER);
        user.setEnabled(false);
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

        Genre genre = new Genre();
        genre.setName("Action");

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

        Genre genre = new Genre();
        genre.setName("Action");

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
        Game game1 = new Game();
        game1.setId(1L);
        game1.setTitle("Title1");

        Game game2 = new Game();
        game2.setId(2L);
        game2.setTitle("Title2");

        List<Game> mockGames = List.of(game1, game2);

        GameDTO gameDTO1 = new GameDTO();
        gameDTO1.setId(1L);
        gameDTO1.setTitle("Title1");

        GameDTO gameDTO2 = new GameDTO();
        gameDTO2.setId(2L);
        gameDTO2.setTitle("Title2");

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
}
