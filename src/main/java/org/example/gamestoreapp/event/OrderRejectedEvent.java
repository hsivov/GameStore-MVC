package org.example.gamestoreapp.event;

import org.example.gamestoreapp.model.dto.OrderResponseDTO;
import org.example.gamestoreapp.model.entity.User;

public class OrderRejectedEvent {
    private final User user;
    private final OrderResponseDTO order;

    public OrderRejectedEvent(User user, OrderResponseDTO order) {
        this.user = user;
        this.order = order;
    }

    public User getUser() {
        return user;
    }

    public OrderResponseDTO getOrder() {
        return order;
    }
}
