package org.example.gamestoreapp.service;

import jakarta.mail.MessagingException;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;

@ExtendWith({MockitoExtension.class})
class OrderServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

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

    @InjectMocks
    @Spy
    private OrderServiceImpl orderService;

    private final String expectedUrl = "https://mock-order-service.com/api/orders";

    private OrderResponseDTO mockOrder1, mockOrder2;
    private BigDecimal price1, price2;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(orderService, "orderServiceUrl", "https://mock-order-service.com");
        ReflectionTestUtils.setField(orderService, "apiKey", "test-api-key");
        ReflectionTestUtils.setField(orderService, "secret", "test-secret");

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
    void testGetAllOrders_Success() throws NoSuchAlgorithmException, InvalidKeyException {

        OrderResponseDTO[] mockResponse = {mockOrder1, mockOrder2};

        ResponseEntity<OrderResponseDTO[]> mockResponseEntity = ResponseEntity.ok(mockResponse);

        when(restTemplate.exchange(eq(expectedUrl), eq(HttpMethod.GET), any(HttpEntity.class), eq(OrderResponseDTO[].class)))
                .thenReturn(mockResponseEntity);

        List<OrderResponseDTO> allOrders = orderService.getAllOrders();

        assertNotNull(allOrders);
        assertEquals(2, allOrders.size());
        assertEquals(price1, allOrders.get(0).getTotalPrice());
        assertEquals(price2, allOrders.get(1).getTotalPrice());

        verify(restTemplate, times(1)).exchange(eq(expectedUrl), eq(HttpMethod.GET), any(HttpEntity.class), eq(OrderResponseDTO[].class));
    }

    @Test
    void testGetAllOrders_EmptyResponse() throws NoSuchAlgorithmException, InvalidKeyException {
        // Mock empty response
        ResponseEntity<OrderResponseDTO[]> mockResponseEntity = ResponseEntity.ok(null);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(OrderResponseDTO[].class)))
                .thenReturn(mockResponseEntity);

        // Call the method
        List<OrderResponseDTO> orders = orderService.getAllOrders();

        // Verify results
        assertNotNull(orders);
        assertTrue(orders.isEmpty());

        // Verify that restTemplate was called once
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(OrderResponseDTO[].class));
    }

    @Test
    void testGetOrderById() throws NoSuchAlgorithmException, InvalidKeyException {
        String orderId = "/1";
        OrderResponseDTO mockResponse = mockOrder1;

        ResponseEntity<OrderResponseDTO> mockResponseEntity = ResponseEntity.ok(mockResponse);

        when(restTemplate.exchange(eq(expectedUrl + orderId), eq(HttpMethod.GET), any(HttpEntity.class), eq(OrderResponseDTO.class)))
                .thenReturn(mockResponseEntity);

        OrderResponseDTO order = orderService.getOrderById(1L);

        assertNotNull(order);
        assertEquals(price1, order.getTotalPrice());

        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(OrderResponseDTO.class));
    }

    @Test
    void testGetOrdersByUser_Success() throws NoSuchAlgorithmException, InvalidKeyException {
        String userId = "/customer/1";
        OrderResponseDTO[] mockResponse = {mockOrder1, mockOrder2};

        ResponseEntity<OrderResponseDTO[]> mockResponseEntity = ResponseEntity.ok(mockResponse);

        when(restTemplate.exchange(eq(expectedUrl + userId), eq(HttpMethod.GET), any(HttpEntity.class), eq(OrderResponseDTO[].class)))
                .thenReturn(mockResponseEntity);

        List<OrderResponseDTO> result = orderService.getOrdersByUser(1L);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(price1, result.get(0).getTotalPrice());
        assertEquals(price2, result.get(1).getTotalPrice());

        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(OrderResponseDTO[].class));
    }

    @Test
    void testGetOrdersByUser_EmptyResponse() throws NoSuchAlgorithmException, InvalidKeyException {
        ResponseEntity<OrderResponseDTO[]> mockResponseEntity = ResponseEntity.ok(null);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(OrderResponseDTO[].class)))
                .thenReturn(mockResponseEntity);

        List<OrderResponseDTO> result = orderService.getOrdersByUser(1L);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(OrderResponseDTO[].class));
    }

    @Test
    void testSendCreateOrderRequest_Success() throws NoSuchAlgorithmException, InvalidKeyException {
        String endpoint = "/create";
        CreateOrderRequestDTO createOrderRequestDTO = new CreateOrderRequestDTO();
        createOrderRequestDTO.setCustomerId(1L);
        createOrderRequestDTO.setTotalPrice(BigDecimal.valueOf(301.98));

        OrderResponseDTO mockResponse = new OrderResponseDTO();
        mockResponse.setTotalPrice(BigDecimal.valueOf(301.98));

        ResponseEntity<OrderResponseDTO> mockResponseEntity = ResponseEntity.ok(mockResponse);

        when(restTemplate.postForEntity(eq(expectedUrl + endpoint), any(HttpEntity.class), eq(OrderResponseDTO.class)))
                .thenReturn(mockResponseEntity);

        OrderResponseDTO result = orderService.sendCreateOrderRequest(createOrderRequestDTO);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(301.98), result.getTotalPrice());

        verify(restTemplate, times(1)).postForEntity(anyString(), any(HttpEntity.class), eq(OrderResponseDTO.class));
    }

    @Test
    void testSendCreateOrderRequest_Failure() throws NoSuchAlgorithmException, InvalidKeyException {
        String endpoint = "/create";
        CreateOrderRequestDTO createOrderRequestDTO = new CreateOrderRequestDTO();
        createOrderRequestDTO.setCustomerId(1L);
        createOrderRequestDTO.setTotalPrice(BigDecimal.valueOf(301.98));

        when(restTemplate.postForEntity(eq(expectedUrl + endpoint), any(HttpEntity.class), eq(OrderResponseDTO.class)))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.BAD_REQUEST));

        Exception exception = assertThrows(RuntimeException.class, () ->
            orderService.sendCreateOrderRequest(createOrderRequestDTO)
        );

        assertEquals("Failed to create order in OrderService", exception.getMessage());
    }

    @Test
    void testProcessPendingOrders_handleOrderApproved() throws MessagingException, NoSuchAlgorithmException, InvalidKeyException {
        String url = expectedUrl + "/pending";

        User mockUser = new User();
        mockUser.setId(1L);

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

        when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), any(HttpEntity.class), eq(OrderResponseDTO[].class)))
                .thenReturn(ResponseEntity.ok(new OrderResponseDTO[]{dto}));

        when(userRepository.findById(dto.getCustomer().getId())).thenReturn(Optional.of(mockUser));
        when(gameRepository.findByIdIn(any())).thenReturn(List.of(new Game()));

        when(orderStatusUpdater.updateOrderStatus()).thenReturn(OrderStatus.APPROVED);

        doNothing().when(orderService).completeOrder(any(), any(), any());

        orderService.processPendingOrders();

        // Assert
        verify(userRepository).findById(1L);
        verify(gameRepository).findByIdIn(Set.of(1005L));
        verify(notificationService, never()).sendNotification(any(), any());
        verify(emailService, never()).sendEmail(any(), any(), any());
    }

    @Test
    void testProcessPendingOrders_handleOrderRejected() throws MessagingException, NoSuchAlgorithmException, InvalidKeyException {
        String url = expectedUrl + "/pending";

        User mockUser = new User();
        mockUser.setId(1L);

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

        when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), any(HttpEntity.class), eq(OrderResponseDTO[].class)))
                .thenReturn(ResponseEntity.ok(new OrderResponseDTO[]{dto}));

        when(userRepository.findById(dto.getCustomer().getId())).thenReturn(Optional.of(mockUser));

        when(orderStatusUpdater.updateOrderStatus()).thenReturn(OrderStatus.REJECTED);

        orderService.processPendingOrders();

        // Assert
        verify(userRepository, times(1)).findById(1L);
        verify(notificationService, times(1)).sendNotification(any(), any());
        verify(emailService, times(1)).sendEmail(any(), any(), any());
    }

    @Test
    void testCompleteOrder() throws MessagingException, NoSuchAlgorithmException, InvalidKeyException {
        String url = expectedUrl + "/update";

        User customer = new User();
        customer.setId(1L);

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

        Game game = new Game();
        game.setId(5L);
        game.setTitle("Game");

        when(restTemplate.postForEntity(eq(url), any(HttpEntity.class), eq(Void.class))).thenReturn(ResponseEntity.status(HttpStatus.OK).build());

        orderService.completeOrder(dto, customer, List.of(game));

        verify(userRepository, times(1)).save(customer); // Verify save called once
        verify(emailService, times(1)).sendEmail(eq(customer.getEmail()), eq("Order Confirmation"), anyString()); // Verify email sent
        verify(notificationService, times(1)).sendNotification(anyString(), eq(customer)); // Verify notification sent
        assertEquals(1, customer.getOwnedGames().size(), "Games should be added to the user's library.");
    }

    @Test
    void testCompleteOrder_shouldNotProceed_whenUpdateOrderRequestFails() throws Exception {
        User mockUser = new User();
        mockUser.setId(1L);

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

        ResponseEntity<Void> failedResponse = ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(failedResponse);

        // Act
        orderService.completeOrder(dto, mockUser, List.of(new Game()));

        // Assert
        verify(userRepository, never()).save(any(User.class)); // Ensure save is not called
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString()); // Ensure email is not sent
        verify(notificationService, never()).sendNotification(anyString(), any()); // Ensure notification is not sent
    }
}
