package org.example.gamestoreapp.service.impl;

import org.example.gamestoreapp.model.dto.OrderDTO;
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
    public List<OrderDTO> getAllOrders() throws NoSuchAlgorithmException, InvalidKeyException {
        HttpHeaders headers = new HttpHeaders();
        String endpoint = "/api/orders";
        String url = orderServiceUrl + endpoint;
        String method = "GET";
        String timestamp = String.valueOf(Instant.now().getEpochSecond());

        String payload = method + endpoint + timestamp;
        String signature = HMACUtil.generateHMAC(payload, secret);

        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Api-Key", apiKey);
        headers.set("X-Signature", signature);
        headers.set("X-Timestamp", timestamp);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<OrderDTO[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, OrderDTO[].class);

        OrderDTO[] responseBody = response.getBody();

        if (responseBody == null) {
            return Collections.emptyList();
        }

        return Arrays.asList(responseBody);
    }
}
