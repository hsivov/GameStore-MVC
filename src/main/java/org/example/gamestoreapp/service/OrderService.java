package org.example.gamestoreapp.service;

import org.example.gamestoreapp.model.dto.OrderDTO;

import java.util.List;

public interface OrderService {
    List<OrderDTO> getAll();
}
