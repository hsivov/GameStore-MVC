package org.example.gamestoreapp.service.impl;

import org.example.gamestoreapp.model.dto.CreateOrderRequestDTO;
import org.example.gamestoreapp.model.dto.OrderItemDTO;
import org.example.gamestoreapp.model.dto.OrderResponseDTO;
import org.example.gamestoreapp.model.entity.ShoppingCart;
import org.example.gamestoreapp.model.entity.User;
import org.example.gamestoreapp.repository.ShoppingCartRepository;
import org.example.gamestoreapp.service.CheckoutService;
import org.example.gamestoreapp.service.NotificationService;
import org.example.gamestoreapp.service.OrderService;
import org.example.gamestoreapp.service.session.UserHelperService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class CheckoutServiceImpl implements CheckoutService {
    private final UserHelperService userHelperService;
    private final ShoppingCartRepository shoppingCartRepository;
    private final OrderService orderService;
    private final NotificationService notificationService;

    public CheckoutServiceImpl(UserHelperService userHelperService,
                               ShoppingCartRepository shoppingCartRepository,
                               OrderService orderService,
                               NotificationService notificationService) {
        this.userHelperService = userHelperService;
        this.shoppingCartRepository = shoppingCartRepository;
        this.orderService = orderService;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public void payment(String paymentMethod) {
        User currentUser = userHelperService.getUser();
        Optional<ShoppingCart> cart = shoppingCartRepository.findByCustomer(currentUser);

        if (cart.isPresent()) {
            ShoppingCart shoppingCart = cart.get();

            List<OrderItemDTO> orderItems = shoppingCart.getGames().stream()
                    .map(game -> {
                        OrderItemDTO dto = new OrderItemDTO();
                        dto.setOrderItemId(game.getId());
                        dto.setTitle(game.getTitle());
                        dto.setPrice(game.getPrice());
                        return dto;
                    })
                    .toList();

            // Prepare request to OrderService
            CreateOrderRequestDTO createOrderRequest = new CreateOrderRequestDTO();
            createOrderRequest.setCustomerId(currentUser.getId());
            createOrderRequest.setOrderItems(orderItems);
            createOrderRequest.setTotalPrice(shoppingCart.getTotalPrice());
            createOrderRequest.setOrderDate(LocalDateTime.now());
            createOrderRequest.setPaymentMethod(paymentMethod);

            // Send request to OrderService
            OrderResponseDTO orderResponse = orderService.sendCreateOrderRequest(createOrderRequest);

            switch (orderResponse.getStatus()) {
                case APPROVED -> {
                    //Confirm Order
                    orderService.completeOrder(orderResponse);

                    // Remove shopping cart from the App
                    shoppingCartRepository.delete(shoppingCart);
                }

                case PENDING -> {
                    String message = "Your order #" + orderResponse.getId() + " is awaiting processing. Your games will be available instantly after payment confirmation.";

                    shoppingCartRepository.delete(shoppingCart);

                    notificationService.sendNotification(message, currentUser);
                }
            }
        }
    }
}
