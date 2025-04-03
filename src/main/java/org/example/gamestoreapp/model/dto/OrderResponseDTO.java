package org.example.gamestoreapp.model.dto;

import org.example.gamestoreapp.model.enums.OrderStatus;

import java.math.BigDecimal;
import java.util.List;

public class OrderResponseDTO {
    private Long id;
    private String orderDate;
    private OrderStatus status;
    private List<OrderItemDTO> boughtGames;
    private BigDecimal totalPrice;
    private UserDTO customer;

    public OrderResponseDTO() {}

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

    public List<OrderItemDTO> getBoughtGames() {
        return boughtGames;
    }

    public void setBoughtGames(List<OrderItemDTO> boughtGames) {
        this.boughtGames = boughtGames;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public UserDTO getCustomer() {
        return customer;
    }

    public void setCustomer(UserDTO customer) {
        this.customer = customer;
    }
}
