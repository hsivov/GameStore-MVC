package org.example.gamestoreapp.service.impl;

import org.example.gamestoreapp.model.dto.OrderDTO;
import org.example.gamestoreapp.repository.OrderRepository;
import org.example.gamestoreapp.service.OrderService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final ModelMapper modelMapper;

    public OrderServiceImpl(OrderRepository orderRepository,
                            ModelMapper modelMapper) {
        this.orderRepository = orderRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public List<OrderDTO> getAll() {
        return orderRepository.findAll().stream()
                .map((order) -> modelMapper.map(order, OrderDTO.class))
                .toList();
    }
}
