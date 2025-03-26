package org.example.gamestoreapp.service;

import org.example.gamestoreapp.model.dto.GameDTO;
import org.example.gamestoreapp.model.dto.ShoppingCartDTO;
import org.example.gamestoreapp.model.entity.Game;
import org.example.gamestoreapp.model.entity.ShoppingCart;
import org.example.gamestoreapp.model.entity.User;
import org.example.gamestoreapp.repository.GameRepository;
import org.example.gamestoreapp.repository.ShoppingCartRepository;
import org.example.gamestoreapp.service.impl.ShoppingCartServiceImpl;
import org.example.gamestoreapp.service.session.UserHelperService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShoppingCartServiceImplTest {

    @Mock
    private UserHelperService userHelperService;

    @Mock
    private ShoppingCartRepository shoppingCartRepository;

    @Mock
    private GameRepository gameRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ShoppingCartServiceImpl shoppingCartService;

    private User mockUser;
    private ShoppingCart mockShoppingCart;
    private Game mockGame;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("username");

        mockGame = new Game();
        mockGame.setId(1L);
        mockGame.setTitle("title");

        mockShoppingCart = new ShoppingCart();
        mockShoppingCart.setId(1L);
    }

    @Test
    void testAddToCart_GivenUser_ShouldAddToCart_CartExists() {
        mockShoppingCart.setGames(new ArrayList<>());
        mockShoppingCart.getGames().add(mockGame);

        Game gameToAdd = new Game();
        gameToAdd.setId(2L);
        gameToAdd.setTitle("Game to add");

        when(userHelperService.getUser()).thenReturn(mockUser);
        when(shoppingCartRepository.findByCustomer(mockUser)).thenReturn(Optional.of(mockShoppingCart));
        when(gameRepository.findById(2L)).thenReturn(Optional.of(gameToAdd));

        shoppingCartService.addToCart(2L);

        assertEquals(2, mockShoppingCart.getGames().size());
        assertEquals("Game to add", mockShoppingCart.getGames().get(1).getTitle());
        assertEquals("username", mockUser.getUsername());

        verify(userHelperService, times(1)).getUser();
        verify(shoppingCartRepository, times(1)).findByCustomer(mockUser);
        verify(gameRepository, times(1)).findById(2L);
        verify(shoppingCartRepository, times(1)).save(mockShoppingCart);
    }

    @Test
    void testAddToCart_GivenUser_ShouldAddToCart_CartNotExists() {
        Game gameToAdd = new Game();
        gameToAdd.setId(2L);
        gameToAdd.setTitle("Game to add");

        when(userHelperService.getUser()).thenReturn(mockUser);
        when(shoppingCartRepository.findByCustomer(mockUser)).thenReturn(Optional.of(mockShoppingCart));
        when(gameRepository.findById(2L)).thenReturn(Optional.of(gameToAdd));

        shoppingCartService.addToCart(2L);

        assertEquals(1, mockShoppingCart.getGames().size());
        assertEquals("Game to add", mockShoppingCart.getGames().get(0).getTitle());
        assertEquals("username", mockUser.getUsername());

        verify(userHelperService, times(1)).getUser();
        verify(shoppingCartRepository, times(1)).findByCustomer(mockUser);
        verify(gameRepository, times(1)).findById(2L);
        verify(shoppingCartRepository, times(1)).save(mockShoppingCart);
    }

    @Test
    void testGetShoppingCart() {
        GameDTO gameDTO = new GameDTO();
        gameDTO.setId(1L);
        gameDTO.setTitle("GameDTO");

        ShoppingCartDTO shoppingCartDTO = new ShoppingCartDTO();
        shoppingCartDTO.setGames(List.of(gameDTO));

        when(userHelperService.getUser()).thenReturn(mockUser);
        when(shoppingCartRepository.findByCustomer(mockUser)).thenReturn(Optional.of(mockShoppingCart));
        when(modelMapper.map(mockShoppingCart, ShoppingCartDTO.class)).thenReturn(shoppingCartDTO);

        shoppingCartService.getShoppingCart();

        verify(userHelperService, times(1)).getUser();
        verify(shoppingCartRepository, times(1)).findByCustomer(mockUser);
        verify(modelMapper, times(1)).map(mockShoppingCart, ShoppingCartDTO.class);
    }

    @Test
    void testRemoveItemFromCart_Success() {
        mockShoppingCart.setGames(new ArrayList<>());
        mockShoppingCart.getGames().add(mockGame);

        when(userHelperService.getUser()).thenReturn(mockUser);
        when(shoppingCartRepository.findByCustomer(mockUser)).thenReturn(Optional.of(mockShoppingCart));
        when(gameRepository.findById(1L)).thenReturn(Optional.of(mockGame));

        shoppingCartService.removeItem(1L);

        assertTrue(mockShoppingCart.getGames().isEmpty());

        verify(userHelperService, times(1)).getUser();
        verify(shoppingCartRepository, times(1)).findByCustomer(mockUser);
        verify(gameRepository, times(1)).findById(1L);
        verify(shoppingCartRepository, times(1)).save(mockShoppingCart);
    }

    @Test
    void testRemoveItemFromCart_ShoppingCartDoesNotExists() {
        when(userHelperService.getUser()).thenReturn(mockUser);
        when(shoppingCartRepository.findByCustomer(mockUser)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> shoppingCartService.removeItem(1L));

        assertEquals("Shopping cart not found", exception.getMessage());

        verify(userHelperService, times(1)).getUser();
        verify(shoppingCartRepository, times(1)).findByCustomer(mockUser);
        verify(gameRepository, never()).findById(1L);
        verify(shoppingCartRepository, never()).save(any(ShoppingCart.class));
    }

    @Test
    void testRemoveItemFromCart_GameDoesNotExists() {
        when(userHelperService.getUser()).thenReturn(mockUser);
        when(shoppingCartRepository.findByCustomer(mockUser)).thenReturn(Optional.of(mockShoppingCart));
        when(gameRepository.findById(1L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> shoppingCartService.removeItem(1L));

        assertEquals("Game not found", exception.getMessage());

        verify(userHelperService, times(1)).getUser();
        verify(shoppingCartRepository, times(1)).findByCustomer(mockUser);
        verify(gameRepository, times(1)).findById(1L);
        verify(shoppingCartRepository, never()).save(any(ShoppingCart.class));
    }

    @Test
    void testRemoveItemFromCart_GameIsNotInShoppingCart() {
        mockShoppingCart.setGames(new ArrayList<>());
        mockShoppingCart.getGames().add(mockGame);

        Game notInCartGame = new Game();
        notInCartGame.setId(99L);
        notInCartGame.setTitle("Game to remove");

        when(userHelperService.getUser()).thenReturn(mockUser);
        when(shoppingCartRepository.findByCustomer(mockUser)).thenReturn(Optional.of(mockShoppingCart));
        when(gameRepository.findById(99L)).thenReturn(Optional.of(notInCartGame));

        Exception exception = assertThrows(RuntimeException.class, () -> shoppingCartService.removeItem(99L));

        assertEquals("Game not found in the cart", exception.getMessage());

        verify(userHelperService, times(1)).getUser();
        verify(shoppingCartRepository, times(1)).findByCustomer(mockUser);
        verify(gameRepository, times(1)).findById(99L);
        verify(shoppingCartRepository, never()).save(any(ShoppingCart.class));
    }

    @Test
    void testRemoveAll_ShoppingCartDoesExists() {
        Game anotherMockGame = new Game();
        anotherMockGame.setId(3L);
        anotherMockGame.setTitle("Test game");

        mockShoppingCart.setGames(new ArrayList<>());
        mockShoppingCart.getGames().add(mockGame);
        mockShoppingCart.getGames().add(anotherMockGame);

        when(userHelperService.getUser()).thenReturn(mockUser);
        when(shoppingCartRepository.findByCustomer(mockUser)).thenReturn(Optional.of(mockShoppingCart));

        shoppingCartService.removeAll();

        assertTrue(mockShoppingCart.getGames().isEmpty());

        verify(userHelperService, times(1)).getUser();
        verify(shoppingCartRepository, times(1)).findByCustomer(mockUser);
        verify(shoppingCartRepository, times(1)).save(mockShoppingCart);
    }

    @Test
    void testRemoveAll_ShoppingCartDoesNotExists() {
        when(userHelperService.getUser()).thenReturn(mockUser);
        when(shoppingCartRepository.findByCustomer(mockUser)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> shoppingCartService.removeAll());

        assertEquals("Shopping cart not found", exception.getMessage());

        verify(userHelperService, times(1)).getUser();
        verify(shoppingCartRepository, times(1)).findByCustomer(mockUser);
        verify(shoppingCartRepository, never()).save(any(ShoppingCart.class));
    }

    @Test
    void testIsGameInCart() {
        mockShoppingCart.setGames(List.of(mockGame));
        when(userHelperService.getUser()).thenReturn(mockUser);
        when(shoppingCartRepository.findByCustomer(mockUser)).thenReturn(Optional.of(mockShoppingCart));
        when(gameRepository.findById(1L)).thenReturn(Optional.of(mockGame));

        boolean result = shoppingCartService.isGameInCart(1L);

        assertTrue(result);

        verify(userHelperService, times(1)).getUser();
        verify(shoppingCartRepository, times(1)).findByCustomer(mockUser);
        verify(gameRepository, times(1)).findById(1L);
    }

    @Test
    void testIsGameInCart_ShoppingCartDoesNotExists_ShouldReturnFalse() {
        mockShoppingCart.setGames(List.of(mockGame));
        when(userHelperService.getUser()).thenReturn(mockUser);
        when(shoppingCartRepository.findByCustomer(mockUser)).thenReturn(Optional.empty());
        when(gameRepository.findById(1L)).thenReturn(Optional.of(mockGame));

        boolean result = shoppingCartService.isGameInCart(1L);

        assertFalse(result);

        verify(userHelperService, times(1)).getUser();
        verify(shoppingCartRepository, times(1)).findByCustomer(mockUser);
        verify(gameRepository, times(1)).findById(1L);
    }

    @Test
    void testIsGameInCart_GameDoesNotExists_ShouldReturnFalse() {
        mockShoppingCart.setGames(List.of(mockGame));
        when(userHelperService.getUser()).thenReturn(mockUser);
        when(shoppingCartRepository.findByCustomer(mockUser)).thenReturn(Optional.of(mockShoppingCart));
        when(gameRepository.findById(1L)).thenReturn(Optional.empty());

        boolean result = shoppingCartService.isGameInCart(1L);

        assertFalse(result);

        verify(userHelperService, times(1)).getUser();
        verify(shoppingCartRepository, times(1)).findByCustomer(mockUser);
        verify(gameRepository, times(1)).findById(1L);
    }
}