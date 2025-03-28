package com.example.finalyearproject.Controllers;

import com.example.finalyearproject.DataStore.Product;
import com.example.finalyearproject.Services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/products")
public class ProductController {
    private ProductService productService;

    //add product
    @PostMapping("/add-product/{farmerId}")
    public ResponseEntity<Product> AddProduct(@RequestBody Product product, @PathVariable("farmerId")int farmerId){
        if (product!=null && farmerId!=0){
            Product storedProduct = this.productService.AddProduct(product,farmerId);
            if(storedProduct==null)
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            return ResponseEntity.ok(storedProduct);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
    // update-product

    @PutMapping("/update-product/{farmerId}")
    public ResponseEntity<Product> UpdateProduct(@RequestBody Product product, int farmerId){
        if(product != null && farmerId != 0){
            Product product1 = this.productService.UpdateProduct(product, farmerId);
            if(product1!=null)
                return ResponseEntity.ok(product1);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    // delete-product

    @DeleteMapping("/delete-product/{farmerId}/{productId}")
    public ResponseEntity DeleteProduct(@PathVariable("farmerId") int farmerId,@PathVariable("productId")int productId){
        if(farmerId!=0 && productId!=0){
            this.productService.DeleteProduct(farmerId, productId);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
}
