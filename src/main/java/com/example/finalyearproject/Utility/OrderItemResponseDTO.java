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
    private String productImage;
    private double unitPrice;
    private int quantity;
    private Unit unit;
    private double totalPrice;
    private FulfillmentStatus status;
    private String farmerName;
    private int farmerId;  // ADD THIS
    private Date deliveredAt;
    private Date confirmedAt;
    private String deliveryNotes;
    private boolean canBeRated;
    private boolean canDonate;  // ADD THIS - true if order is delivered/confirmed
}