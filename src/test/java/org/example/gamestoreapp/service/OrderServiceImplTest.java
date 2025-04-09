package org.example.gamestoreapp.service;

import org.example.gamestoreapp.model.dto.CreateOrderRequestDTO;
import org.example.gamestoreapp.model.dto.OrderItemDTO;
import org.example.gamestoreapp.model.dto.OrderResponseDTO;
import org.example.gamestoreapp.model.dto.UserDTO;
import org.example.gamestoreapp.model.entity.Game;
import org.example.gamestoreapp.model.entity.User;
import org.example.gamestoreapp.model.enums.OrderStatus;
import org.example.gamestoreapp.repository.GameRepository;
import org.example.gamestoreapp.repository.UserRepository;
import org.example.gamestoreapp.service.impl.OrderServiceImpl;
import org.example.gamestoreapp.update.OrderStatusUpdater;
import org.example.gamestoreapp.util.OrderServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class})
class OrderServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private GameRepository gameRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private OrderStatusUpdater orderStatusUpdater;

    @Mock
    private OrderServiceClient orderServiceClient;

    @InjectMocks
    private OrderServiceImpl orderService;

    private User mockUser;
    private OrderResponseDTO mockOrder1, mockOrder2;
    private BigDecimal price1, price2;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);

        price1 = new BigDecimal("100.00");
        price2 = new BigDecimal("200.00");

        mockOrder1 = new OrderResponseDTO();
        mockOrder1.setId(1L);
        mockOrder1.setTotalPrice(price1);

        mockOrder2 = new OrderResponseDTO();
        mockOrder2.setId(2L);
        mockOrder2.setTotalPrice(price2);
    }

    @Test
    void testGetAllOrders_Success() {

        OrderResponseDTO[] mockResponse = {mockOrder1, mockOrder2};

        when(orderServiceClient.get(anyString(), anyString(), any())).thenReturn(mockResponse);

        List<OrderResponseDTO> allOrders = orderService.getAllOrders();

        assertNotNull(allOrders);
        assertEquals(2, allOrders.size());
        assertEquals(price1, allOrders.get(0).getTotalPrice());
        assertEquals(price2, allOrders.get(1).getTotalPrice());

        verify(orderServiceClient, times(1)).get(anyString(), anyString(), any());
    }

    @Test
    void testGetOrderById() {
        long orderId = 1L;

        when(orderServiceClient.get(anyString(), anyString(), any())).thenReturn(mockOrder1);

        OrderResponseDTO order = orderService.getOrderById(orderId);

        assertNotNull(order);
        assertEquals(price1, order.getTotalPrice());

        verify(orderServiceClient, times(1)).get(anyString(), anyString(), any());
    }

    @Test
    void testGetOrdersByUser_Success() {
        long userId = 1L;

        OrderResponseDTO[] mockResponse = {mockOrder1, mockOrder2};
        when(orderServiceClient.get(anyString(), anyString(), any())).thenReturn(mockResponse);

        List<OrderResponseDTO> result = orderService.getOrdersByUser(userId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(price1, result.get(0).getTotalPrice());
        assertEquals(price2, result.get(1).getTotalPrice());

        verify(orderServiceClient, times(1)).get(anyString(), anyString(), any());
    }

    @Test
    void testSendCreateOrderRequest_Success() {
        CreateOrderRequestDTO requestDTO = new CreateOrderRequestDTO();

        when(orderServiceClient.post(anyString(), eq(requestDTO), eq(OrderResponseDTO.class))).thenReturn(mockOrder1);

        OrderResponseDTO result = orderService.sendCreateOrderRequest(requestDTO);

        assertNotNull(result);
        assertEquals(1L, result.getId());

        verify(orderServiceClient, times(1)).post(anyString(), eq(requestDTO), eq(OrderResponseDTO.class));
    }

    @Test
    void testProcessPendingOrders_handleOrderApproved() {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setFirstName("First");
        userDTO.setLastName("Last");

        OrderItemDTO mockOrderItemDTO = new OrderItemDTO();
        mockOrderItemDTO.setOrderItemId(1005L);
        mockOrderItemDTO.setTitle("Game");
        mockOrderItemDTO.setPrice(BigDecimal.valueOf(49.99));

        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setId(199L);
        dto.setCustomer(userDTO);
        dto.setBoughtGames(List.of(mockOrderItemDTO));

        OrderResponseDTO[] mockResponse = {dto};

        when(orderServiceClient.get(anyString(), anyString(), any())).thenReturn(mockResponse);
        when(userRepository.findById(dto.getCustomer().getId())).thenReturn(Optional.of(mockUser));
        when(gameRepository.findByIdIn(any())).thenReturn(List.of(new Game()));

        when(orderStatusUpdater.updateOrderStatus()).thenReturn(OrderStatus.APPROVED);

        orderService.processPendingOrders();

        // Assert
        verify(notificationService, times(1)).sendNotification(any(), any());
        verify(emailService, times(1)).sendEmail(any(), any(), any());
    }

    @Test
    void testProcessPendingOrders_handleOrderRejected() {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setFirstName("First");
        userDTO.setLastName("Last");

        OrderItemDTO mockOrderItemDTO = new OrderItemDTO();
        mockOrderItemDTO.setOrderItemId(1005L);
        mockOrderItemDTO.setTitle("Game");
        mockOrderItemDTO.setPrice(BigDecimal.valueOf(49.99));

        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setId(199L);
        dto.setCustomer(userDTO);
        dto.setBoughtGames(List.of(mockOrderItemDTO));

        OrderResponseDTO[] mockResponse = {dto};

        when(orderServiceClient.get(anyString(), anyString(), any())).thenReturn(mockResponse);

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        when(orderStatusUpdater.updateOrderStatus()).thenReturn(OrderStatus.REJECTED);

        orderService.processPendingOrders();

        // Assert
        verify(userRepository, times(1)).findById(1L);
        verify(notificationService, times(1)).sendNotification(any(), any());
        verify(emailService, times(1)).sendEmail(any(), any(), any());
    }

    @Test
    void testCompleteOrder() {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setFirstName("First");
        userDTO.setLastName("Last");

        OrderItemDTO orderItemDTO = new OrderItemDTO();
        orderItemDTO.setOrderItemId(1005L);
        orderItemDTO.setTitle("Game");
        orderItemDTO.setPrice(BigDecimal.valueOf(49.99));

        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setId(199L);
        dto.setCustomer(userDTO);
        dto.setBoughtGames(List.of(orderItemDTO));

        User customer = new User();
        customer.setId(1L);

        when(userRepository.findById(dto.getCustomer().getId())).thenReturn(Optional.of(customer));
        when(gameRepository.findByIdIn(any())).thenReturn(List.of(new Game()));
        orderService.completeOrder(dto);

        assertEquals(1, customer.getOwnedGames().size(), "Games should be added to the user's library.");

        verify(gameRepository, times(1)).findByIdIn(Set.of(1005L));
        verify(userRepository, times(1)).save(customer); // Verify save called once
        verify(emailService, times(1)).sendEmail(eq(customer.getEmail()), eq("Order Confirmation"), anyString()); // Verify email sent
        verify(notificationService, times(1)).sendNotification(anyString(), eq(customer)); // Verify notification sent
    }
}
