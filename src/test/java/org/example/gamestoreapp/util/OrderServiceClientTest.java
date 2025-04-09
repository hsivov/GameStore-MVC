package org.example.gamestoreapp.util;

import org.example.gamestoreapp.model.dto.CreateOrderRequestDTO;
import org.example.gamestoreapp.model.dto.OrderResponseDTO;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private OrderServiceClient orderServiceClient;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(orderServiceClient, "orderServiceUrl", "https://mock-order-service.com");
        ReflectionTestUtils.setField(orderServiceClient, "apiKey", "test-api-key");
        ReflectionTestUtils.setField(orderServiceClient, "secret", "test-secret");
    }

    @Test
    void testGet_shouldReturnOrderResponseDTO() {
        OrderResponseDTO expectedResponse = new OrderResponseDTO();

        ResponseEntity<OrderResponseDTO> responseEntity = new ResponseEntity<>(expectedResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(OrderResponseDTO.class))).thenReturn(responseEntity);

        OrderResponseDTO result = orderServiceClient.get("/api/orders", "GET", OrderResponseDTO.class);

        assertNotNull(result);
        assertEquals(expectedResponse, result);
    }
    @Test
    void testPost_shouldPostOrderSuccessfully() {
        CreateOrderRequestDTO request = new CreateOrderRequestDTO();
        OrderResponseDTO expectedResponse = new OrderResponseDTO();

        ResponseEntity<OrderResponseDTO> responseEntity = new ResponseEntity<>(expectedResponse, HttpStatus.OK);

        when(restTemplate.postForEntity(
                anyString(),
                any(HttpEntity.class),
                eq(OrderResponseDTO.class)
        )).thenReturn(responseEntity);

        OrderResponseDTO result = orderServiceClient.post("/orders", request, OrderResponseDTO.class);

        assertNotNull(result);
        assertEquals(expectedResponse, result);
    }
}