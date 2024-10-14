package org.example.gamestoreapp.model.dto;

import org.example.gamestoreapp.model.entity.User;
import org.example.gamestoreapp.model.enums.OrderStatus;

import java.math.BigDecimal;

public class OrderDTO {
    private Long id;
    private String orderDate;
    private OrderStatus status;
    private BigDecimal totalPrice;
    private User customer;

    public OrderDTO() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public User getCustomer() {
        return customer;
    }

    public void setCustomer(User customer) {
        this.customer = customer;
    }
}
