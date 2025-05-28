package com.example.finalyearproject.Controllers;

import com.example.finalyearproject.DataStore.CategoryType;
import com.example.finalyearproject.DataStore.Product;
import com.example.finalyearproject.DataStore.ProductImage;
import com.example.finalyearproject.DataStore.Unit;
import com.example.finalyearproject.Services.ProductImageService;
import com.example.finalyearproject.Services.ProductService;
import com.example.finalyearproject.Utility.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductImageService productImageService;

//    @GetMapping("products/farmer/{farmerId}")
//    public ResponseEntity<ApiResponse<List<Product>>> getProductsByFarmer(@PathVariable int farmerId) {
//        ApiResponse<List<Product>> response = productService.getProductsByFarmerId(farmerId);
//        return ResponseEntity.ok(response);
//    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('FARMER')")
    public ResponseEntity<ApiResponse<ProductResponseDTO>> addProduct(
            @ModelAttribute @Valid ProductUtility productUtility,
            Authentication authentication) {

        String farmerEmail = authentication.getName();
        ApiResponse<ProductResponseDTO> response = productService.AddProduct(productUtility, farmerEmail);

        if (response.getData() != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @DeleteMapping("/{productId}")
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


    @PutMapping("/{productId}")
    @PreAuthorize("hasAuthority('FARMER')")
    public ResponseEntity<ApiResponse<ProductResponseDTO>> updateProduct(
            @PathVariable int productId,
            @RequestBody @Valid ProductUpdateDTO productUpdateDTO,
            Authentication authentication) {

        String farmerEmail = authentication.getName();
        ApiResponse<ProductResponseDTO> response = productService.updateProduct(productUpdateDTO, productId, farmerEmail);

        if (response.getData() != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/units")
    public ResponseEntity<ApiResponse<List<UnitDTO>>> getAvailableUnits() {
        try{
            List<UnitDTO> units = Arrays.stream(Unit.values())
                    .map(unit -> new UnitDTO(unit.name(), unit.getDisplayName()))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success("Available units retrieved successfully", units));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve units", e.getMessage()));
        }
    }


    @PostMapping(value = "/images/{productId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('FARMER')")
    public ResponseEntity<ApiResponse<List<ProductImage>>> addProductImages(
            @PathVariable int productId,
            @RequestParam("images") MultipartFile[] images,
            Authentication authentication) {

        String farmerEmail = authentication.getName();

        // First verify product belongs to this farmer
        ApiResponse<Product> productResponse = productService.getProductByIdAndFarmerEmail(productId, farmerEmail);

        if (productResponse.getData() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied", "You don't have permission to modify this product"));
        }

        ApiResponse<List<ProductImage>> response = productImageService.uploadProductImages(productId, images);

        if (response.getData() != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Delete a specific product image (farmer only)
     */
    @DeleteMapping("images/{imageId}")
    @PreAuthorize("hasAuthority('FARMER')")
    public ResponseEntity<ApiResponse<Void>> deleteProductImage(
            @PathVariable int imageId,
            Authentication authentication) {

        String farmerEmail = authentication.getName();

        // First verify image belongs to this farmer's product
        ApiResponse<Boolean> ownershipResponse = productImageService.verifyImageOwnership(imageId, farmerEmail);

        if (ownershipResponse.getData() == null || !ownershipResponse.getData()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied", "You don't have permission to delete this image"));
        }

        ApiResponse<Void> response = productImageService.deleteProductImage(imageId);

        if (response.getErrors() == null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    @GetMapping("/categories")
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
    // Helper method to capitalize first letter and lowercase the rest
    private String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    /**
     * Reset the random ordering (useful for forcing a new shuffle)
     */
    @PostMapping("/random/reset")
    public ResponseEntity<ApiResponse<String>> resetRandomOrder() {
        ApiResponse<String> response = productService.resetRandomOrder();
        return ResponseEntity.ok(response);
    }
}