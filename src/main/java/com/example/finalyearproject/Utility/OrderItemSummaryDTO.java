package com.example.finalyearproject.Utility;

import com.example.finalyearproject.DataStore.FulfillmentStatus;
import lombok.Data;

@Data
public class OrderItemSummaryDTO {
    private int orderItemId;
    private int productId;
    private String productName;
    private String productImage;
    private FulfillmentStatus status;
    private double totalPrice;
    private int quantity;
}
