package org.example.gamestoreapp.update;

import org.example.gamestoreapp.model.enums.OrderStatus;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class RandomOrderStatusUpdater implements OrderStatusUpdater {
    @Override
    public OrderStatus updateOrderStatus() {
        OrderStatus[] values = OrderStatus.values();
        return values[new Random().nextInt(values.length)];
    }
}
