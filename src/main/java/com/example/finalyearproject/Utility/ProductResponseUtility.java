package com.example.finalyearproject.Utility;

import com.example.finalyearproject.DataStore.Unit;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
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
    private Unit unit;  // ADD THIS FIELD
    private LocalDate harvestDate;
    private LocalDate availableDate;
    private boolean isOrganic;  // Also add this for completeness
    private List<String> imageUrls;
}