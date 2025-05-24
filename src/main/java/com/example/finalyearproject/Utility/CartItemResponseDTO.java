package com.example.finalyearproject.Utility;

import lombok.Data;

@Data
public class CartItemResponseDTO {
    private int orderItemId;
    private int productId;
    private String productName;
    private String productImage;  // Main product image
    private int quantity;
    private double unitPrice;
    private double totalPrice;
    private String farmerName;
    private String fieldChange;  // For tracking changes
}
