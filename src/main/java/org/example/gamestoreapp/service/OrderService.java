package org.example.gamestoreapp.service;

import org.example.gamestoreapp.model.dto.OrderDTO;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public interface OrderService {
    List<OrderDTO> getAllOrders() throws NoSuchAlgorithmException, InvalidKeyException;
}
