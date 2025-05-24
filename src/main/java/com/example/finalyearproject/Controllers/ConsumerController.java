package com.example.finalyearproject.Controllers;

import com.example.finalyearproject.DataStore.*;
import com.example.finalyearproject.Services.ConsumerService;
import com.example.finalyearproject.Services.OrderService;
import com.example.finalyearproject.Services.ProductService;
import com.example.finalyearproject.Services.RatingServices;
import com.example.finalyearproject.Utility.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/consumer")
public class ConsumerController {

    @Autowired
    private ProductService productService;

    @Autowired
    private RatingServices ratingServices;


    // Existing product-related methods remain unchanged
    @GetMapping("/products/all")
    @PreAuthorize("hasAuthority('CONSUMER')")
    public ResponseEntity<ApiResponse<List<Product>>> getAllProducts() {
        ApiResponse<List<Product>> response = productService.getAllProducts();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/products/{productId}")
    @PreAuthorize("hasAuthority('CONSUMER')")
    public ResponseEntity<ApiResponse<Product>> getProductById(@PathVariable int productId) {
        ApiResponse<Product> response = productService.getProductById(productId);

        if (response.getData() != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @GetMapping("products/category/{category}")
    @PreAuthorize("hasAuthority('CONSUMER')")
    public ResponseEntity<ApiResponse<List<Product>>> getProductsByCategory(@PathVariable String category) {
        ApiResponse<List<Product>> response = productService.getProductsByCategory(category);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/products")
    @PreAuthorize("hasAuthority('CONSUMER')")
    public ResponseEntity<ApiResponse<Page<Product>>> getProductsForConsumer(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        ApiResponse<Page<Product>> response = productService.getRandomProductsPaginated(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("products/search")
    @PreAuthorize("hasAuthority('CONSUMER')")
    public ResponseEntity<ApiResponse<List<Product>>> searchProducts(@RequestParam String query) {
        ApiResponse<List<Product>> response = productService.searchProductsByName(query);
        return ResponseEntity.ok(response);
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
}