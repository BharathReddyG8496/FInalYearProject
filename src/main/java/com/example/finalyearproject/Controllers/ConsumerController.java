package com.example.finalyearproject.Controllers;


import com.example.finalyearproject.DataStore.CategoryType;
import com.example.finalyearproject.DataStore.Product;
import com.example.finalyearproject.DataStore.Rating;
import com.example.finalyearproject.Services.ProductService;
import com.example.finalyearproject.Services.RatingServices;
import com.example.finalyearproject.Utility.ApiResponse;
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


    @GetMapping("/products")
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


}