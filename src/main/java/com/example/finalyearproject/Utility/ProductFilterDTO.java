package com.example.finalyearproject.Utility;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductFilterDTO {
    // Pagination
    @Builder.Default
    private int page = 0;

    @Builder.Default
    private int size = 10;

    // Filter criteria
    private Double minPrice;
    private Double maxPrice;
    private String category;
    private String searchTerm;
    private Boolean isOrganic;
    private Integer farmerId;
    private Double minRating;

    // Sorting
    private String sortBy;
}