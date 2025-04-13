package com.example.finalyearproject.Utility;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ProductResponseUtility {
    private int productId;
    private String name;
    private String description;
    private double price;
    private int stock;
    private String category;
    private List<String> imageUrls;
}
