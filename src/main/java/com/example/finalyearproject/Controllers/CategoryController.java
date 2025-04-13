package com.example.finalyearproject.Controllers;

import com.example.finalyearproject.Abstraction.ProductRepo;
import com.example.finalyearproject.DataStore.CategoryType;
import com.example.finalyearproject.DataStore.Product;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    private ProductRepo productRepo;

    @GetMapping("/get-category-items/{category}")
    public ResponseEntity<List<Product>> getProductsOfCategory(@PathVariable String category) {
        List<Product> byCategory = productRepo.findByCategory(CategoryType.valueOf(category));
        if(byCategory.isEmpty()){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(byCategory);
    }
}
