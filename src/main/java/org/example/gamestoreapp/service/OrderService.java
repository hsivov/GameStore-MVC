package org.example.gamestoreapp.service;

import org.example.gamestoreapp.model.dto.CreateOrderRequestDTO;
import org.example.gamestoreapp.model.dto.OrderResponseDTO;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public interface OrderService {
    List<OrderResponseDTO> getAllOrders() throws NoSuchAlgorithmException, InvalidKeyException;

    OrderResponseDTO getOrderById(long id) throws NoSuchAlgorithmException, InvalidKeyException;

    List<OrderResponseDTO> getOrdersByUser(long userId) throws NoSuchAlgorithmException, InvalidKeyException;

    OrderResponseDTO sendCreateOrderRequest(CreateOrderRequestDTO createOrderRequest) throws NoSuchAlgorithmException, InvalidKeyException;
}
