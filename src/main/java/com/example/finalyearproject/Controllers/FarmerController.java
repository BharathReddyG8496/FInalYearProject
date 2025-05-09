package com.example.finalyearproject.Controllers;

import com.example.finalyearproject.Abstraction.FarmerRepo;
import com.example.finalyearproject.DataStore.Farmer;
import com.example.finalyearproject.DataStore.Product;
import com.example.finalyearproject.DataStore.ProductImage;
import com.example.finalyearproject.Services.FarmerService;
import com.example.finalyearproject.Services.ProductService;
import com.example.finalyearproject.Utility.ApiResponse;
import com.example.finalyearproject.Utility.ProductResponseUtility;
import com.example.finalyearproject.Utility.ProductUtility;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/farmer")
public class FarmerController {

    @Autowired
    private FarmerRepo farmerRepo;

    @Autowired
    private FarmerService farmerService;

    @Autowired
    private ProductService productService;

    @PostMapping("/product")
    @PreAuthorize("hasAuthority('FARMER')")
    public ResponseEntity<ApiResponse<Product>> AddProduct(@Valid @ModelAttribute ProductUtility prodUtil) {
        String farmerEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        ApiResponse<Product> response = productService.AddProduct(prodUtil, farmerEmail);

        if (response.getData() != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // update-farmer
    @PutMapping("/update-farmer")
    @PreAuthorize("hasAuthority('FARMER')")
    public ResponseEntity<ApiResponse<Farmer>> UpdateFarmer(@Valid @RequestBody Farmer farmer) {
        String farmerEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        ApiResponse<Farmer> response = farmerService.UpdateFarmer(farmer, farmerEmail);

        if (response.getData() != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/my-products")
    @PreAuthorize("hasAuthority('FARMER')")
    public ResponseEntity<ApiResponse<List<Product>>> getMyProducts(Authentication authentication) {
        String farmerEmail = authentication.getName();
        ApiResponse<List<Product>> response = productService.getProductsByFarmerEmail(farmerEmail);
        return ResponseEntity.ok(response);
    }

    // Alternative formatted response if you prefer ProductResponseUtility
    @GetMapping("/products-formatted")
    @PreAuthorize("hasAuthority('FARMER')")
    public ResponseEntity<ApiResponse<Set<ProductResponseUtility>>> GetAllProductsFormatted() {
        String farmerEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        ApiResponse<List<Product>> productsResponse = productService.getProductsByFarmerEmail(farmerEmail);

        if (productsResponse.getData() != null) {
            Set<ProductResponseUtility> formattedProducts = productsResponse.getData().stream()
                    .map(product -> ProductResponseUtility.builder()
                            .productId(product.getProductId())
                            .description(product.getDescription())
                            .stock(product.getStock())
                            .price(product.getPrice())
                            .name(product.getName())
                            .category(product.getCategory().toString())
                            .imageUrls(product.getImages().stream()
                                    .map(ProductImage::getFilePath) // No need to hardcode localhost
                                    .collect(Collectors.toList()))
                            .build())
                    .collect(Collectors.toSet());

            return ResponseEntity.ok(ApiResponse.success("Products retrieved successfully", formattedProducts));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Failed to retrieve products", productsResponse.getMessage()));
        }
    }

    @DeleteMapping("/{productId}")
    @PreAuthorize("hasAuthority('FARMER')")
    public ResponseEntity<ApiResponse<Void>> DeleteProduct(@PathVariable("productId") int productId) {
        String farmerEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        ApiResponse<Void> response = productService.DeleteProduct(productId, farmerEmail);

        if (response.getErrors() == null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping(value = "/product/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('FARMER')")
    public ResponseEntity<ApiResponse<Product>> addProduct(
            @ModelAttribute @Valid ProductUtility productUtility,
            Authentication authentication) {

        String farmerEmail = authentication.getName();
        ApiResponse<Product> response = productService.AddProduct(productUtility, farmerEmail);

        if (response.getData() != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Delete a product (farmer only)
     */
    @DeleteMapping("/product/delete/{productId}")
    @PreAuthorize("hasAuthority('FARMER')")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @PathVariable int productId,
            Authentication authentication) {

        String farmerEmail = authentication.getName();
        ApiResponse<Void> response = productService.DeleteProduct(productId, farmerEmail);

        if (response.getErrors() == null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<String>> exceptionHandler() {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Authentication failed", "Invalid credentials"));
    }
}