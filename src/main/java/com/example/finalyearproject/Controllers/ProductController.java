package com.example.finalyearproject.Controllers;

import com.example.finalyearproject.DataStore.Product;
import com.example.finalyearproject.Services.ProductImageService;
import com.example.finalyearproject.Services.ProductService;
import com.example.finalyearproject.Utility.ProductUpdateDTO;
import com.example.finalyearproject.Utility.ProductUtility;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductImageService productImageService;

    //add product
    @PostMapping("/add-product")
    public ResponseEntity<?> AddProduct(@Valid @ModelAttribute ProductUtility prodUtil){
        String farmerEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            Product storedProduct = productService.AddProduct(prodUtil, farmerEmail);
            return ResponseEntity.ok(storedProduct);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to add product.");
        }
    }
    // update-product
    @PutMapping("/update-product/{productId}")
    public ResponseEntity<?> updateProduct(
            @Valid @ModelAttribute ProductUpdateDTO dto,
            @PathVariable int productId) {

        // Get the currently authenticated farmer's email from security context.
        String farmerEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        try {
            Product updatedProduct = productService.updateProduct(dto, productId, farmerEmail);
            return ResponseEntity.ok(updatedProduct);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update product: " + ex.getMessage());
        }
    }

    @PostMapping("/{productId}/add-image")
    public ResponseEntity<String> addImage(@PathVariable int productId,
                                           @RequestParam("image") MultipartFile[] files) {
        try {
            productImageService.uploadProductImages(productId, files);
            return ResponseEntity.ok("Image uploaded successfully.");
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error uploading image: " + ex.getMessage());
        }
    }

    // delete-product

    @DeleteMapping("/delete-product/{productId}")
    public ResponseEntity<?> DeleteProduct(@PathVariable("productId")int productId){
        String farmerEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        try{
            this.productService.DeleteProduct(productId, farmerEmail);
            return ResponseEntity.ok().build();
        }catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
}
