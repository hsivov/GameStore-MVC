package org.example.gamestoreapp.service;

import org.example.gamestoreapp.model.dto.GameDTO;
import org.example.gamestoreapp.model.entity.Game;
import org.example.gamestoreapp.model.entity.User;
import org.example.gamestoreapp.repository.GameRepository;
import org.example.gamestoreapp.repository.UserRepository;
import org.example.gamestoreapp.service.impl.GameServiceImpl;
import org.example.gamestoreapp.service.session.UserHelperService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GameServiceImplTest {
    @Mock
    private GameRepository gameRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private UserHelperService userHelperService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApiService steamApiService;

    @InjectMocks
    private GameServiceImpl gameService;

    private Game game1, game2, game3;
    private GameDTO gameDTO1, gameDTO2;
    private List<Game> mockGames;
    private User mockUser;

    @BeforeEach
    public void setUp() {
        game1 = new Game();
        game1.setId(1L);
        game1.setTitle("Game One");
        game1.setAppId(123456);

        game2 = new Game();
        game2.setId(2L);
        game2.setTitle("Game Two");
        game2.setAppId(789012);

        game3 = new Game();
        game3.setId(3L);
        game3.setTitle("Game Three");
        game3.setAppId(1000);

        mockGames = List.of(game1, game2);

        gameDTO1 = new GameDTO();
        gameDTO1.setId(1L);
        gameDTO1.setTitle("Game One");

        gameDTO2 = new GameDTO();
        gameDTO2.setId(2L);
        gameDTO2.setTitle("Game Two");

        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("username");
        mockUser.setOwnedGames(mockGames);
    }

    @Test
    void testGetAllGames() {

        when(gameRepository.findAll()).thenReturn(mockGames);
        when(modelMapper.map(game1, GameDTO.class)).thenReturn(gameDTO1);
        when(modelMapper.map(game2, GameDTO.class)).thenReturn(gameDTO2);

        List<GameDTO> result = gameService.getAll();

        verify(gameRepository, times(1)).findAll();
        verify(modelMapper, times(2)).map(any(Game.class), eq(GameDTO.class));

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Game One", result.get(0).getTitle());
        assertEquals("Game Two", result.get(1).getTitle());
    }

    @Test
    void testGetOwnedGames() {
        when(userHelperService.getUser()).thenReturn(mockUser);
        when(modelMapper.map(game1, GameDTO.class)).thenReturn(gameDTO1);
        when(modelMapper.map(game2, GameDTO.class)).thenReturn(gameDTO2);

        List<GameDTO> result = gameService.getOwnedGames();

        verify(userHelperService, times(1)).getUser();
        verify(modelMapper, times(2)).map(any(Game.class), eq(GameDTO.class));

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Game One", result.get(0).getTitle());
        assertEquals("Game Two", result.get(1).getTitle());
    }

    @Test
    void testAddToLibrary_GameExist() {
        mockUser.setOwnedGames(new ArrayList<>());

        when(userHelperService.getUser()).thenReturn(mockUser);
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game1));

        gameService.addToLibrary(1L);

        assertEquals(1, mockUser.getOwnedGames().size());
        assertEquals("Game One", mockUser.getOwnedGames().get(0).getTitle());

        verify(userHelperService, times(1)).getUser();
        verify(gameRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testAddToLibrary_GameNotExist() {
        mockUser.setOwnedGames(new ArrayList<>());

        when(userHelperService.getUser()).thenReturn(mockUser);
        when(gameRepository.findById(1L)).thenReturn(Optional.empty());

        gameService.addToLibrary(1L);

        assertTrue(mockUser.getOwnedGames().isEmpty());

        verify(userHelperService, times(1)).getUser();
        verify(gameRepository, times(1)).findById(1L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testGetGameById() {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game1));
        when(modelMapper.map(game1, GameDTO.class)).thenReturn(gameDTO1);

        Optional<GameDTO> result = gameService.getGameById(1L);

        assertTrue(result.isPresent());
        assertEquals("Game One", result.get().getTitle());

        verify(gameRepository, times(1)).findById(1L);
        verify(modelMapper, times(1)).map(game1, GameDTO.class);
    }

    @Test
    void testGetGamesByIds() {
        Set<Long> mockGameIds = Set.of(1L, 2L);
        when(gameRepository.findByIdIn(mockGameIds)).thenReturn(mockGames);
        when(modelMapper.map(game1, GameDTO.class)).thenReturn(gameDTO1);
        when(modelMapper.map(game2, GameDTO.class)).thenReturn(gameDTO2);

        List<GameDTO> result = gameService.getGamesByIds(mockGameIds);

        verify(gameRepository, times(1)).findByIdIn(mockGameIds);
        verify(modelMapper, times(2)).map(any(Game.class), eq(GameDTO.class));

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Game One", result.get(0).getTitle());
        assertEquals("Game Two", result.get(1).getTitle());
    }

    @Test
    public void testUpdatePrices() {
        mockGames = List.of(game1, game2, game3);
        when(gameRepository.findAll()).thenReturn(mockGames);
        when(steamApiService.fetchPrice(123456)).thenReturn(Mono.just(BigDecimal.valueOf(49.99)));
        when(steamApiService.fetchPrice(789012)).thenReturn(Mono.just(BigDecimal.valueOf(19.99)));
        when(steamApiService.fetchPrice(1000)).thenReturn(null);

        gameService.updatePricesAt0010();

        // Game 1 should be updated
        assertEquals(BigDecimal.valueOf(49.99), game1.getPrice());
        // Game 2 should be updated
        assertEquals(BigDecimal.valueOf(19.99), game2.getPrice());
        // Game 3 should be untouched
        assertNull(game3.getPrice());

        verify(steamApiService, times(3)).fetchPrice(anyInt());
        verify(gameRepository, times(1)).saveAll(anyList());
    }
}
