package org.example.gamestoreapp.service.impl;

import org.example.gamestoreapp.model.dto.OrderResponseDTO;
import org.example.gamestoreapp.service.OrderService;
import org.example.gamestoreapp.util.HMACUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {
    private final RestTemplate restTemplate;

    @Value("${order.service.url}")
    private String orderServiceUrl;

    @Value("${app.api.key}")
    private String apiKey;

    @Value("${app.api.secret}")
    private String secret;

    public OrderServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public List<OrderResponseDTO> getAllOrders() throws NoSuchAlgorithmException, InvalidKeyException {

        String endpoint = "/api/orders";
        String url = orderServiceUrl + endpoint;
        String method = "GET";

        HttpHeaders headers = setHeaders(method, endpoint);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<OrderResponseDTO[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, OrderResponseDTO[].class);

        OrderResponseDTO[] responseBody = response.getBody();

        if (responseBody == null) {
            return Collections.emptyList();
        }

        return Arrays.asList(responseBody);
    }

    @Override
    public OrderResponseDTO getOrderById(long id) throws NoSuchAlgorithmException, InvalidKeyException {
        String endpoint = "/api/orders/" + id;
        String url = orderServiceUrl + endpoint;
        String method = "GET";

        HttpHeaders headers = setHeaders(method, endpoint);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<OrderResponseDTO> response = restTemplate.exchange(url, HttpMethod.GET, entity, OrderResponseDTO.class);

        return response.getBody();
    }

    private HttpHeaders setHeaders(String method, String endpoint) throws NoSuchAlgorithmException, InvalidKeyException {
        HttpHeaders headers = new HttpHeaders();
        String timestamp = String.valueOf(Instant.now().getEpochSecond());

        String payload = method + endpoint + timestamp;
        String signature = HMACUtil.generateHMAC(payload, secret);

        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Api-Key", apiKey);
        headers.set("X-Signature", signature);
        headers.set("X-Timestamp", timestamp);

        return headers;
    }
}
