package com.example.finalyearproject.Controllers;


import com.example.finalyearproject.DataStore.Rating;
import com.example.finalyearproject.Services.RatingServices;
import com.example.finalyearproject.customExceptions.ResourceNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Set;

@RestController
@RequestMapping("/rating")
public class RatingController {

    @Autowired
    private RatingServices ratingServices;

    @PostMapping("/add-rating/{consumerId}/{productId}")
    public ResponseEntity<?> addRating(@Valid @RequestBody Rating rating, @PathVariable("consumerId") int consumerId, @PathVariable("productId") int productId){
        try {
            rating.setTimestamp(LocalDateTime.now());
            return new ResponseEntity<>(ratingServices.addRating(rating, consumerId, productId), HttpStatus.CREATED);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PutMapping("/edit-rating/{consumerId}")
    public ResponseEntity<?> updateRating(@Valid @RequestBody Rating rating, @PathVariable int  consumerId){
        try {
           return new ResponseEntity<>( ratingServices.updateRating(rating,consumerId),HttpStatus.OK);
        }catch (Exception e){
            System.out.println(e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/delete-rating/{consumerId}/{ratingId}")
    public ResponseEntity<?> deleteRating(@PathVariable int ratingId, @PathVariable int  consumerId){
        try {
            ratingServices.deleteRating(ratingId,consumerId);
           return new ResponseEntity<>(HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/get-consumer-ratings/{consumerId}")
    public ResponseEntity<?> getConsumerRatings(@PathVariable int consumerId){
       try {
           Set<Rating> consumerRatings = ratingServices.getConsumerRatings(consumerId);
           return ResponseEntity.ok(consumerRatings);
       }catch (IllegalArgumentException e){
           return new ResponseEntity<>(e.getMessage(),HttpStatus.BAD_REQUEST);
       }
    }
    @GetMapping("/get-product-ratings/{productId}")
    public ResponseEntity<?> getProductRatings(@PathVariable int productId){
        try {
            Set<Rating> productRatings = ratingServices.getProductRatings(productId);
            return ResponseEntity.ok(productRatings);
        }catch (IllegalArgumentException e){
            return new ResponseEntity<>(e.getMessage(),HttpStatus.BAD_REQUEST);
        }
    }
}
