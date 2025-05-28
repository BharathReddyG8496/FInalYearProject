package com.example.finalyearproject.Utility;

import com.example.finalyearproject.DataStore.CategoryType;
import com.example.finalyearproject.DataStore.Unit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDTO {
    private int productId;
    private String name;
    private String description;
    private double price;
    private int stock;
    private LocalDate harvestDate;
    private LocalDate availableFromDate;
    private boolean isOrganic;
    private CategoryType category;
    private Unit unit;
    private Double averageRating;
    private Integer ratingCount;

    // Farmer details
    private int farmerId;
    private String farmerName;
    private String farmerEmail;
    private String farmerPhone;
    private Double farmerRating;

    // Images
    private Set<ProductImageDTO> images;
}