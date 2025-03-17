package org.example.gamestoreapp.service;

import org.example.gamestoreapp.model.dto.CreateOrderRequestDTO;
import org.example.gamestoreapp.model.dto.OrderResponseDTO;
import org.example.gamestoreapp.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

@ExtendWith({MockitoExtension.class})
class OrderServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
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
}