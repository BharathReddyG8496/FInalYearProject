package com.example.finalyearproject.Utility;

import com.example.finalyearproject.DataStore.OrderStatus;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class OrderResponseDTO {
    private int orderId;
    private OrderStatus status;
    private double totalAmount;
    private Date createdAt;
    private Date placedAt;
    private String shippingAddress;
    private String shippingCity;
    private String shippingState;
    private String shippingZip;

    // Add consumer information
    private ConsumerInfoDTO consumer;

    private List<OrderItemResponseDTO> items;
}