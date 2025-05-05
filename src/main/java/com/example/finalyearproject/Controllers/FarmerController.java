package com.example.finalyearproject.Controllers;

import com.example.finalyearproject.Abstraction.FarmerRepo;
import com.example.finalyearproject.Abstraction.ProductRepo;
import com.example.finalyearproject.DataStore.Farmer;
import com.example.finalyearproject.DataStore.Product;
import com.example.finalyearproject.Model.JwtRequest;
import com.example.finalyearproject.Model.JwtResponse;
import com.example.finalyearproject.Security.JwtHelper;
import com.example.finalyearproject.Services.FarmerService;
import com.example.finalyearproject.Services.ProductService;
import com.example.finalyearproject.Utility.FarmerUtility;
import com.example.finalyearproject.Utility.ProductResponseUtility;
import com.example.finalyearproject.Utility.ProductUtility;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
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

    // update-farmer
    @PutMapping("/update-farmer/{farmerId}")
    public ResponseEntity<Optional<Farmer>> UpdateFarmer(@Valid @RequestBody Farmer farmer, @PathVariable("farmerId")int farmerId){
        String FarmerName = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println(FarmerName);
        try{
            Optional<Farmer> farmer1 = this.farmerService.UpdateFarmer(farmer,FarmerName);
            if(farmer1.isPresent())
                return ResponseEntity.ok(farmer1);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @GetMapping("/get-all-farmer-products")
    public ResponseEntity<?> GetAllProducts(){
        String farmerEmail = SecurityContextHolder.getContext().getAuthentication().getName();
       try{
           Farmer byFarmerEmail = farmerRepo.findByFarmerEmail(farmerEmail);
           Set<Product> farmerProducts = byFarmerEmail.getFarmerProducts();
           Set<ProductResponseUtility> response = new HashSet<>();
           for(Product product:farmerProducts){
               response.add(ProductResponseUtility.builder().productId(product.getProductId()).
                       description(product.getDescription()).stock(product.getStock()).price(product.getPrice()).name(product.getName()).category(product.getCategory().toString()).imageUrls(
                               product.getImages().stream()
                                       .map(img -> "http://localhost:8081" + img.getFilePath())
                                       .collect(Collectors.toList())
                       ).build());
           }
           return new ResponseEntity<>(response, HttpStatus.OK);

       }catch (Exception e){
           return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
       }
    }

    @ExceptionHandler(BadCredentialsException.class)
    public String exceptionHandler() {
        return "Credentials Invalid !!";
    }

}
