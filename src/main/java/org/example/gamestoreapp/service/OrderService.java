package org.example.gamestoreapp.service;

import org.example.gamestoreapp.model.dto.CreateOrderRequestDTO;
import org.example.gamestoreapp.model.dto.OrderResponseDTO;

import java.util.List;

public interface OrderService {
    List<OrderResponseDTO> getAllOrders();

    OrderResponseDTO getOrderById(long id);

    List<OrderResponseDTO> getOrdersByUser(long userId);

    OrderResponseDTO sendCreateOrderRequest(CreateOrderRequestDTO createOrderRequest);

    void completeOrder(OrderResponseDTO order);
}
