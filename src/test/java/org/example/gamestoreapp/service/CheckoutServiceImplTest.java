package org.example.gamestoreapp.service;

import jakarta.mail.MessagingException;
import org.example.gamestoreapp.model.dto.CreateOrderRequestDTO;
import org.example.gamestoreapp.model.dto.OrderResponseDTO;
import org.example.gamestoreapp.model.entity.Game;
import org.example.gamestoreapp.model.entity.ShoppingCart;
import org.example.gamestoreapp.model.entity.User;
import org.example.gamestoreapp.repository.ShoppingCartRepository;
import org.example.gamestoreapp.repository.UserRepository;
import org.example.gamestoreapp.service.impl.CheckoutServiceImpl;
import org.example.gamestoreapp.service.session.UserHelperService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CheckoutServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private ShoppingCartRepository shoppingCartRepository;

    @Mock
    private UserHelperService userHelperService;

    @Mock
    private EmailService emailService;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private CheckoutServiceImpl checkoutService;

    private User mockUser;
    private List<Game> mockGames;
    private ShoppingCart mockCart;
    private OrderResponseDTO mockOrderResponse;

    @BeforeEach
    public void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");
        mockUser.setOwnedGames(new ArrayList<>());

        Game game1 = new Game();
        game1.setId(101L);
        game1.setPrice(BigDecimal.valueOf(199.99));
        Game game2 = new Game();
        game2.setId(102L);
        game2.setPrice(BigDecimal.valueOf(299.99));

        mockGames = List.of(game1, game2);

        mockCart = new ShoppingCart();
        mockCart.setGames(mockGames);

        mockOrderResponse = new OrderResponseDTO();
        mockOrderResponse.setId(123L);
    }

    @Test
    void testPayment_Success() throws MessagingException, NoSuchAlgorithmException, InvalidKeyException {
        when(userHelperService.getUser()).thenReturn(mockUser);
        when(shoppingCartRepository.findByCustomer(mockUser)).thenReturn(Optional.of(mockCart));
        when(orderService.sendCreateOrderRequest(any(CreateOrderRequestDTO.class))).thenReturn(mockOrderResponse);

        checkoutService.payment("CREDIT_CARD");

        assertTrue(mockUser.getOwnedGames().containsAll(mockGames), "User should own the purchased games");

        verify(orderService, times(1)).sendCreateOrderRequest(any(CreateOrderRequestDTO.class));
        verify(emailService, times(1)).sendEmail(eq(mockUser.getEmail()), anyString(), anyString());
        verify(shoppingCartRepository, times(1)).delete(mockCart);
        verify(userRepository, times(1)).save(mockUser);
    }

    @Test
    void testPayment_CartNotPresent() throws MessagingException, NoSuchAlgorithmException, InvalidKeyException {
        when(userHelperService.getUser()).thenReturn(mockUser);
        when(shoppingCartRepository.findByCustomer(mockUser)).thenReturn(Optional.empty());

        checkoutService.payment("PAYPAL");

        verify(orderService, never()).sendCreateOrderRequest(any());
        verify(emailService, never()).sendEmail(any(), any(), any());
        verify(shoppingCartRepository, never()).delete(any());
    }

    @Test
    void testPayment_EmailFailure() throws MessagingException, NoSuchAlgorithmException, InvalidKeyException {
        when(userHelperService.getUser()).thenReturn(mockUser);
        when(shoppingCartRepository.findByCustomer(mockUser)).thenReturn(Optional.of(mockCart));
        when(orderService.sendCreateOrderRequest(any(CreateOrderRequestDTO.class))).thenReturn(mockOrderResponse);

        doThrow(new MessagingException("Email error")).when(emailService).sendEmail(any(), any(), any());

        assertThrows(MessagingException.class, () -> checkoutService.payment("CREDIT_CARD"));

        verify(shoppingCartRepository, never()).delete(mockCart);
        verify(userRepository, never()).save(mockUser);
    }
}
