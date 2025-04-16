package com.example.finalyearproject.Utility;

import com.example.finalyearproject.DataStore.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class ProductUpdateDTO {
    @NotBlank(message = "Product name is required")
    private String name;

    @NotBlank(message = "Product description is required")
    private String description;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private Double price;

    @NotNull(message = "Stock is required")
    @PositiveOrZero(message = "Stock must be zero or more")
    private Integer stock;

    @NotNull(message = "Category is required")
    private CategoryType category;
}