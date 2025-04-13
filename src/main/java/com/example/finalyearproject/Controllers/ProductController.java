package com.example.finalyearproject.Controllers;

import com.example.finalyearproject.DataStore.Product;
import com.example.finalyearproject.Services.ProductImageService;
import com.example.finalyearproject.Services.ProductService;
import com.example.finalyearproject.Utility.ProductUtility;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    @PostMapping("/add-product/{farmerId}")
    public ResponseEntity<Product> AddProduct(@Valid @ModelAttribute ProductUtility prodUtil, @PathVariable("farmerId")int farmerId, @RequestParam(value = "images", required = false) MultipartFile[] images){
        if (farmerId!=0){
            Product product = new Product();
            product.setName(prodUtil.getName());
            product.setDescription(prodUtil.getDescription());
            product.setPrice(prodUtil.getPrice());
            product.setStock(prodUtil.getStock());
            Product storedProduct = this.productService.AddProduct(product,farmerId,prodUtil.getCategory());
            if(storedProduct==null)
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

            // If image files are provided, process the upload.
            if (images != null && images.length > 0) {
                try {
                    productImageService.uploadProductImages(storedProduct.getProductId(), images);
                } catch (Exception e) {
                    // Here you could roll back the product insert or just return an error.
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(storedProduct);
                }
            }
            return ResponseEntity.ok(storedProduct);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
    // update-product
    @PutMapping("/update-product/{productId}/{farmerId}")
    public ResponseEntity<Product> UpdateProduct(@Valid @RequestBody Product product,@PathVariable int productId,@PathVariable int farmerId){
        if(farmerId != 0){
            Product product1 = this.productService.UpdateProduct(product,productId, farmerId);
            if(product1!=null)
                return ResponseEntity.ok(product1);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
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

    @DeleteMapping("/delete-product/{farmerId}/{productId}")
    public ResponseEntity<?> DeleteProduct(@PathVariable("farmerId") int farmerId,@PathVariable("productId")int productId){
        if(farmerId!=0 && productId!=0){
            this.productService.DeleteProduct(farmerId, productId);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
}
