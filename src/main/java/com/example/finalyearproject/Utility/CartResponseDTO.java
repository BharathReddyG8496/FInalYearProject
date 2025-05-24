package com.example.finalyearproject.Utility;

import lombok.Data;
import java.util.List;

@Data
public class CartResponseDTO {
    private int cartId;
    private double totalAmount;
    private List<CartItemResponseDTO> items;
    private int itemCount;
}

