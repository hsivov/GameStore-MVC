package org.example.gamestoreapp.service.impl;

import org.example.gamestoreapp.model.dto.OrderDTO;
import org.example.gamestoreapp.service.OrderService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {
    private final RestTemplate restTemplate;

    @Value("${order-service.url}")
    private String orderServiceUrl;

    public OrderServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public List<OrderDTO> getAllOrders() {
        OrderDTO[] orders = restTemplate.getForObject(orderServiceUrl, OrderDTO[].class);

        if (orders == null) {
            return Collections.emptyList();
        }

        return Arrays.asList(orders);
    }
}
