package org.example.gamestoreapp.update;

import org.example.gamestoreapp.model.enums.OrderStatus;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.*;

class RandomOrderStatusUpdaterTest {
    @Test
    void updateOrderStatus_shouldReturnValidEnumValue() {
        RandomOrderStatusUpdater updater = new RandomOrderStatusUpdater();
        OrderStatus result = updater.updateOrderStatus();

        assertNotNull(result, "Returned OrderStatus should not be null");
        assertTrue(EnumSet.allOf(OrderStatus.class).contains(result), "Result should be a valid OrderStatus value");
    }
}