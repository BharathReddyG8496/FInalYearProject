package com.example.finalyearproject.Utility;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductUtility {

    @NotNull
    @NotBlank(message = " product name should not be null ")
    private String name;

    @NotNull
    @NotBlank(message = "description should not be null")
    private String description;

    @NotNull
    private double price;

    @NotNull
    private int stock;

    @NotNull
    @NotBlank(message = "category should not be null")
    private String category;

    // NEW FIELD: Unit
    @NotNull
    @NotBlank(message = "unit should not be null")
    private String unit;

    @NotNull(message = "Harvest Date is required")
    private LocalDate harvestDate;

    @NotNull(message = "available from is required is required")
    private LocalDate availableFromDate;

    @NotNull(message = "Organic field is required")
    private boolean isOrganic;

    @NotNull
    private MultipartFile[] images;
}