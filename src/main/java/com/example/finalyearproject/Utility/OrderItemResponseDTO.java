package com.example.finalyearproject.Utility;

import com.example.finalyearproject.DataStore.FulfillmentStatus;
import com.example.finalyearproject.DataStore.Unit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponseDTO {
    private int orderItemId;
    private int productId;
    private String productName;
    private String productImage;  // Main product image URL
    private double unitPrice;
    private int quantity;
    private Unit unit;  // ADD THIS FIELD
    private double totalPrice;
    private FulfillmentStatus status;
    private String farmerName;
    private Date deliveredAt;
    private Date confirmedAt;
    private String deliveryNotes;
    private boolean canBeRated;
}