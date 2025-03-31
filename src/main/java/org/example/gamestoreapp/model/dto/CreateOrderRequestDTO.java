package org.example.gamestoreapp.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

public class CreateOrderRequestDTO {
    private Long customerId;
    private Map<String, BigDecimal> orderItems;
    private BigDecimal totalPrice;
    private LocalDateTime orderDate;
    private String paymentMethod;

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Map<String, BigDecimal> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(Map<String, BigDecimal> orderItems) {
        this.orderItems = orderItems;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
