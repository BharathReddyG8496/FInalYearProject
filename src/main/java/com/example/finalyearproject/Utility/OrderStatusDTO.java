package com.example.finalyearproject.Utility;

import com.example.finalyearproject.DataStore.OrderStatus;
import lombok.Data;

@Data
public class OrderStatusDTO {
    private int orderId;
    private OrderStatus status;
}