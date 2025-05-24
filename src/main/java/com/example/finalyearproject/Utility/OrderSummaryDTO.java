package com.example.finalyearproject.Utility;

import com.example.finalyearproject.DataStore.OrderStatus;
import lombok.Data;

import java.util.Date;
import java.util.Map;

@Data
public class OrderSummaryDTO {
    private int orderId;
    private OrderStatus status;
    private double totalAmount;
    private Date createdAt;
    private Date placedAt;
    private String shippingAddress;
    private String shippingCity;
    private String shippingState;
    private String shippingZip;

    // Summary counts of items by status
    private Map<String, Integer> itemCounts; // e.g., {"PENDING": 2, "DELIVERED": 1}
}