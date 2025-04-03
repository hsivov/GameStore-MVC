package org.example.gamestoreapp.service;

import jakarta.mail.MessagingException;
import org.example.gamestoreapp.model.dto.CreateOrderRequestDTO;
import org.example.gamestoreapp.model.dto.OrderResponseDTO;
import org.example.gamestoreapp.model.entity.Game;
import org.example.gamestoreapp.model.entity.User;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public interface OrderService {
    List<OrderResponseDTO> getAllOrders() throws NoSuchAlgorithmException, InvalidKeyException;

    OrderResponseDTO getOrderById(long id) throws NoSuchAlgorithmException, InvalidKeyException;

    List<OrderResponseDTO> getOrdersByUser(long userId) throws NoSuchAlgorithmException, InvalidKeyException;

    OrderResponseDTO sendCreateOrderRequest(CreateOrderRequestDTO createOrderRequest) throws NoSuchAlgorithmException, InvalidKeyException;

    void completeOrder(OrderResponseDTO order, User customer, List<Game> games) throws MessagingException, NoSuchAlgorithmException, InvalidKeyException;
}
