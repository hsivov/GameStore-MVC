package org.example.gamestoreapp.service;

import jakarta.mail.MessagingException;
import org.example.gamestoreapp.model.dto.CreateOrderRequestDTO;
import org.example.gamestoreapp.model.dto.OrderResponseDTO;
import org.example.gamestoreapp.model.entity.Game;
import org.example.gamestoreapp.model.entity.ShoppingCart;
import org.example.gamestoreapp.model.entity.User;
import org.example.gamestoreapp.model.enums.OrderStatus;
import org.example.gamestoreapp.repository.ShoppingCartRepository;
import org.example.gamestoreapp.service.impl.CheckoutServiceImpl;
import org.example.gamestoreapp.service.session.UserHelperService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CheckoutServiceImplTest {

    @Mock
    private ShoppingCartRepository shoppingCartRepository;

    @Mock
    private UserHelperService userHelperService;

    @Mock
    private OrderService orderService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private CheckoutServiceImpl checkoutService;

    private User mockUser;
    private ShoppingCart mockCart;
    private OrderResponseDTO mockOrderResponse;

    @BeforeEach
    public void setUp() {
        mockUser = new User();
        mockUser.setId(1L);

        Game mockGame = new Game();
        mockGame.setId(101L);
        mockGame.setTitle("Test Game");
        mockGame.setPrice(BigDecimal.valueOf(29.99));

        mockCart = new ShoppingCart();
        mockCart.setCustomer(mockUser);
        mockCart.setGames(List.of(mockGame));

        mockOrderResponse = new OrderResponseDTO();
        mockOrderResponse.setId(5001L);
        mockOrderResponse.setStatus(OrderStatus.APPROVED);
    }

    @Test
    void testPayment_OrderApproved() throws MessagingException, NoSuchAlgorithmException, InvalidKeyException {
        when(userHelperService.getUser()).thenReturn(mockUser);
        when(shoppingCartRepository.findByCustomer(mockUser)).thenReturn(Optional.of(mockCart));
        when(orderService.sendCreateOrderRequest(any(CreateOrderRequestDTO.class)))
                .thenReturn(mockOrderResponse);

        checkoutService.payment("Credit Card");

        verify(orderService).sendCreateOrderRequest(any(CreateOrderRequestDTO.class));
        verify(orderService).completeOrder(eq(mockOrderResponse), eq(mockUser), anyList());
        verify(shoppingCartRepository).delete(mockCart);
        verifyNoInteractions(notificationService);
    }

    @Test
    void testPayment_OrderPending() throws MessagingException, NoSuchAlgorithmException, InvalidKeyException {

        mockOrderResponse.setStatus(OrderStatus.PENDING);

        when(userHelperService.getUser()).thenReturn(mockUser);
        when(shoppingCartRepository.findByCustomer(mockUser)).thenReturn(Optional.of(mockCart));
        when(orderService.sendCreateOrderRequest(any(CreateOrderRequestDTO.class)))
                .thenReturn(mockOrderResponse);

        checkoutService.payment("PayPal");

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(notificationService).sendNotification(messageCaptor.capture(), eq(mockUser));

        assertTrue(messageCaptor.getValue().contains("Your order #5001 is awaiting processing"));

        verify(shoppingCartRepository).delete(mockCart);
        verify(orderService, never()).completeOrder(any(), any(), any());
    }

    @Test
    void testPayment_NoShoppingCart() throws MessagingException, NoSuchAlgorithmException, InvalidKeyException {
        when(userHelperService.getUser()).thenReturn(mockUser);
        when(shoppingCartRepository.findByCustomer(mockUser)).thenReturn(Optional.empty());

        checkoutService.payment("Crypto");

        verify(orderService, never()).sendCreateOrderRequest(any());
        verify(orderService, never()).completeOrder(any(), any(), any());
        verify(shoppingCartRepository, never()).delete(any());
        verifyNoInteractions(notificationService);
    }

    @Test
    void testPayment_ThrowsMessagingException() throws NoSuchAlgorithmException, InvalidKeyException {
        when(userHelperService.getUser()).thenReturn(mockUser);
        when(shoppingCartRepository.findByCustomer(mockUser)).thenReturn(Optional.of(mockCart));
        when(orderService.sendCreateOrderRequest(any(CreateOrderRequestDTO.class)))
                .thenThrow(new InvalidKeyException("The key is invalid"));

        assertThrows(InvalidKeyException.class, () -> checkoutService.payment("Bank Transfer"));

        verify(orderService).sendCreateOrderRequest(any());
    }
}
