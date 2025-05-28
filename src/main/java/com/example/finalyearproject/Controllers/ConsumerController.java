package com.example.finalyearproject.Controllers;

import com.example.finalyearproject.DataStore.*;
import com.example.finalyearproject.Services.ProductService;
import com.example.finalyearproject.Services.RatingServices;
import com.example.finalyearproject.Utility.ApiResponse;
import com.example.finalyearproject.Utility.CategoryDTO;
import com.example.finalyearproject.Utility.ProductFilterDTO;
import com.example.finalyearproject.Utility.ProductResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/consumer")
public class ConsumerController {

    @Autowired
    private ProductService productService;

    @Autowired
    private RatingServices ratingServices;


    @GetMapping("/products/{productId}")
    public ResponseEntity<ApiResponse<ProductResponseDTO>> getProductById(@PathVariable int productId) {
        ApiResponse<ProductResponseDTO> response = productService.getProductById(productId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @GetMapping("/getCategories")
    @PreAuthorize("hasAuthority('CONSUMER')")
    public ResponseEntity<ApiResponse<List<String>>> getCategoryList(){
        List<String> categories=new ArrayList<>();
        for (CategoryType value : CategoryType.values()) {
            categories.add(value.toString());
        }
        return ResponseEntity.ok(ApiResponse.success("categories fetched successfully",categories));
    }

    // Existing rating-related methods remain unchanged
    @GetMapping("/my-ratings")
    @PreAuthorize("hasAuthority('CONSUMER')")
    public ResponseEntity<ApiResponse<Set<Rating>>> getMyRatings(Authentication authentication) {
        String consumerEmail = authentication.getName();
        ApiResponse<Set<Rating>> response = ratingServices.getUserRatings(consumerEmail);
        if (response.getData() != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
    @GetMapping("/products")
    @PreAuthorize("hasAuthority('CONSUMER')")
    public ResponseEntity<ApiResponse<Page<ProductResponseDTO>>> getProducts(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "minPrice", required = false) Double minPrice,
            @RequestParam(name = "maxPrice", required = false) Double maxPrice,
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "searchTerm", required = false) String searchTerm,
            @RequestParam(name = "isOrganic", required = false) Boolean isOrganic,
            @RequestParam(name = "farmerId", required = false) Integer farmerId,
            @RequestParam(name = "minRating", required = false) Double minRating,
            @RequestParam(name = "sortBy", required = false) String sortBy) {

        ProductFilterDTO filterDTO = ProductFilterDTO.builder()
                .page(page)
                .size(size)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .category(category)
                .searchTerm(searchTerm)
                .isOrganic(isOrganic)
                .farmerId(farmerId)
                .minRating(minRating)
                .sortBy(sortBy)
                .build();

        ApiResponse<Page<ProductResponseDTO>> response = productService.getProducts(filterDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/categories")
    @PreAuthorize("hasAuthority('CONSUMER')")
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getAvailableCategories() {
        try {
            List<CategoryDTO> categories = Arrays.stream(CategoryType.values())
                    .map(category -> new CategoryDTO(
                            category.name(),
                            category.getDisplayName()
                    ))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success("Available categories retrieved successfully", categories));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve categories", e.getMessage()));
        }
    }
}