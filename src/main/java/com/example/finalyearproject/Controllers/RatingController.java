package com.example.finalyearproject.Controllers;


import com.example.finalyearproject.Abstraction.ConsumerRepo;
import com.example.finalyearproject.DataStore.Consumer;
import com.example.finalyearproject.DataStore.Rating;
import com.example.finalyearproject.Services.RatingServices;
import com.example.finalyearproject.customExceptions.ResourceNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Set;

@RestController
@RequestMapping("/rating")
public class RatingController {

    @Autowired
    private RatingServices ratingServices;

    @Autowired
    private ConsumerRepo consumerRepo;

    @PostMapping("/add-rating/{productId}")
    public ResponseEntity<?> addRating(@Valid @RequestBody Rating rating,@PathVariable("productId") int productId){
        String consumerEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            rating.setTimestamp(LocalDateTime.now());
            return new ResponseEntity<>(ratingServices.addRating(rating, consumerEmail, productId), HttpStatus.CREATED);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PutMapping("/edit-rating")
    public ResponseEntity<?> updateRating(@Valid @RequestBody Rating rating){
        String consumerEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
           return new ResponseEntity<>( ratingServices.updateRating(rating,consumerEmail),HttpStatus.OK);
        }catch (Exception e){
            System.out.println(e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/delete-rating/{consumerId}/{ratingId}")
    public ResponseEntity<?> deleteRating(@PathVariable int ratingId, @PathVariable int  consumerId){
        String consumerEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            ratingServices.deleteRating(ratingId,consumerEmail);
           return new ResponseEntity<>(HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/get-consumer-ratings/")
    public ResponseEntity<?> getConsumerRatings(){
        Consumer consumer = consumerRepo.findByConsumerEmail(SecurityContextHolder.getContext().getAuthentication().getName());
        try {
           Set<Rating> consumerRatings = consumer.getConsumerRatings();
           return ResponseEntity.ok(consumerRatings);
       }catch (IllegalArgumentException e){
           return new ResponseEntity<>(e.getMessage(),HttpStatus.BAD_REQUEST);
       }
    }

}
