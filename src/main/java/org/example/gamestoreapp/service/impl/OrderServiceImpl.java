package org.example.gamestoreapp.service.impl;

import org.example.gamestoreapp.model.dto.OrderDTO;
import org.example.gamestoreapp.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {
    private final RestTemplate restTemplate;
    private static final String ORDER_SERVICE_URL = "http://localhost:8081/api/orders";

    public OrderServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public List<OrderDTO> getAllOrders() {
        OrderDTO[] orders = restTemplate.getForObject(ORDER_SERVICE_URL, OrderDTO[].class);

        if (orders == null) {
            return Collections.emptyList();
        }

        return Arrays.asList(orders);
    }
}
