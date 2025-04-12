package org.example.gamestoreapp.service.impl;

import org.example.gamestoreapp.event.OrderApprovedEvent;
import org.example.gamestoreapp.event.OrderRejectedEvent;
import org.example.gamestoreapp.exception.UserNotFoundException;
import org.example.gamestoreapp.model.dto.CreateOrderRequestDTO;
import org.example.gamestoreapp.model.dto.OrderItemDTO;
import org.example.gamestoreapp.model.dto.OrderResponseDTO;
import org.example.gamestoreapp.model.entity.Game;
import org.example.gamestoreapp.model.entity.User;
import org.example.gamestoreapp.model.enums.OrderStatus;
import org.example.gamestoreapp.repository.GameRepository;
import org.example.gamestoreapp.repository.UserRepository;
import org.example.gamestoreapp.service.OrderService;
import org.example.gamestoreapp.update.OrderStatusUpdater;
import org.example.gamestoreapp.util.OrderServiceClient;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {
    private final ApplicationEventPublisher eventPublisher;
    private final GameRepository gameRepository;
    private final UserRepository userRepository;
    private final OrderStatusUpdater orderStatusUpdater;
    private final OrderServiceClient orderServiceClient;

    public OrderServiceImpl(ApplicationEventPublisher eventPublisher,
                            GameRepository gameRepository, UserRepository userRepository,
                            OrderStatusUpdater orderStatusUpdater,
                            OrderServiceClient orderServiceClient) {
        this.eventPublisher = eventPublisher;
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
        this.orderStatusUpdater = orderStatusUpdater;
        this.orderServiceClient = orderServiceClient;
    }

    @Override
    public List<OrderResponseDTO> getAllOrders() {
        OrderResponseDTO[] orders = orderServiceClient.get("/api/orders", "GET", OrderResponseDTO[].class);

        return Arrays.asList(orders);
    }

    @Override
    public OrderResponseDTO getOrderById(long id) {
        return orderServiceClient.get("/api/orders/" + id, "GET", OrderResponseDTO.class);
    }

    @Override
    public List<OrderResponseDTO> getOrdersByUser(long userId) {
        OrderResponseDTO[] orders = orderServiceClient.get("/api/orders/customer/" + userId, "GET", OrderResponseDTO[].class);

        return Arrays.asList(orders);
    }

    @Override
    public OrderResponseDTO sendCreateOrderRequest(CreateOrderRequestDTO requestBody) {
        // Make POST request to OrderService createOrder endpoint and return the OrderResponseDTO
        return orderServiceClient.post("/api/orders/create", requestBody, OrderResponseDTO.class);
    }

    @Scheduled(cron = "0 0/5 * * * ?")
    public void processPendingOrders() {
        OrderResponseDTO[] orders = orderServiceClient.get("/api/orders/pending", "GET", OrderResponseDTO[].class);

        for (OrderResponseDTO order : orders) {
            OrderStatus newStatus = orderStatusUpdater.updateOrderStatus();
            order.setStatus(newStatus);

            if (newStatus == OrderStatus.APPROVED) {
                completeOrder(order);
                updateOrderStatus(order);
            } else if (newStatus == OrderStatus.REJECTED) {
                User customer = userRepository.findById(order.getCustomer().getId())
                        .orElseThrow(() -> new UserNotFoundException("Customer not found"));

                updateOrderStatus(order);

                eventPublisher.publishEvent(new OrderRejectedEvent(customer, order));
            }
        }
    }

    @Override
    public void completeOrder(OrderResponseDTO order) {
        User customer = userRepository.findById(order.getCustomer().getId())
                .orElseThrow(() -> new UserNotFoundException("Customer not found"));

        Set<Long> gameIds = order.getBoughtGames().stream()
                .map(OrderItemDTO::getOrderItemId)
                .collect(Collectors.toSet());

        List<Game> boughtGames = gameRepository.findByIdIn(gameIds);
        customer.getOwnedGames().addAll(boughtGames);

        // Update user with owned games
        userRepository.save(customer);

        // Send confirmation email &
        // Send notification
        eventPublisher.publishEvent(new OrderApprovedEvent(customer, order));
    }

    private void updateOrderStatus(OrderResponseDTO order) {

        orderServiceClient.post("/api/orders/update", order, Void.class);
    }
}
