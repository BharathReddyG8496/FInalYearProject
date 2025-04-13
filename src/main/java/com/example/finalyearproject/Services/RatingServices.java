package com.example.finalyearproject.Services;

import com.example.finalyearproject.Abstraction.ConsumerRepo;
import com.example.finalyearproject.Abstraction.FarmerRepo;
import com.example.finalyearproject.Abstraction.ProductRepo;
import com.example.finalyearproject.Abstraction.RatingRepo;
import com.example.finalyearproject.DataStore.Consumer;
import com.example.finalyearproject.DataStore.Farmer;
import com.example.finalyearproject.DataStore.Product;
import com.example.finalyearproject.DataStore.Rating;
import com.example.finalyearproject.customExceptions.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Set;

@Service
public class RatingServices {

    @Autowired
    private RatingRepo ratingRepo;

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private ConsumerRepo consumerRepo;

    @Autowired
    private FarmerRepo farmerRepo;

    // Helper method to update the farmer's aggregates incrementally on add/update/delete
    private void updateFarmerAggregates(Farmer farmer, double scoreDelta, int countDelta) {
        // Get current aggregates, ensuring they are not null
        double currentTotal = farmer.getTotalRating();
        int currentCount = farmer.getRatingCount();

        currentTotal += scoreDelta;
        currentCount += countDelta;

        double newAverage = currentCount > 0 ? currentTotal / currentCount : 0.0;

        farmer.setTotalRating(currentTotal);
        farmer.setRatingCount(currentCount);
        farmer.setAverageRating(newAverage);

        farmerRepo.save(farmer);
    }
    private void updateProductAggregates(Product product, double scoreDelta, int countDelta) {
        // Get current aggregates, ensuring they are not null
        double currentTotal = product.getTotalRating();
        int currentCount = product.getRatingCount();

        currentTotal += scoreDelta;
        currentCount += countDelta;

        double newAverage = currentCount > 0 ? currentTotal / currentCount : 0.0;

        product.setTotalRating(currentTotal);
        product.setRatingCount(currentCount);
        product.setAverageRating(newAverage);

        productRepo.save(product);
    }

    @Transactional
    public Rating addRating(Rating rating, int consumerId, int productId) throws ResourceNotFoundException {
        Consumer consumer = consumerRepo.findById(consumerId)
                .orElseThrow(() -> new ResourceNotFoundException("Consumer not found with ID: " + consumerId));
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        rating.setConsumer(consumer);
        rating.setProduct(product);
        Rating savedRating = ratingRepo.save(rating);

        Farmer farmer = product.getFarmer();
        if (farmer != null) {
            // Increment aggregates with the new rating's score
            updateFarmerAggregates(farmer, savedRating.getScore(), 1);
            updateProductAggregates(product,savedRating.getScore(), 1);
        }
        return savedRating;
    }

    @Transactional
    public Rating updateRating(Rating updatedRating, int consumerId) throws ResourceNotFoundException {
        Consumer consumer = consumerRepo.findById(consumerId)
                .orElseThrow(() -> new ResourceNotFoundException("Consumer not found with ID: " + consumerId));

        Rating existingRating = ratingRepo.findById(updatedRating.getRatingId())
                .orElseThrow(() -> new ResourceNotFoundException("Rating not found with ID: " + updatedRating.getRatingId()));

        if (existingRating.getConsumer() == null || existingRating.getConsumer().getConsumerId() != consumerId) {
            throw new IllegalArgumentException("Rating does not belong to consumer with ID: " + consumerId);
        }

        // Capture the old score to determine the delta
        double oldScore = existingRating.getScore();
        existingRating.setScore(updatedRating.getScore());
        existingRating.setComment(updatedRating.getComment());
        Rating savedRating = ratingRepo.save(existingRating);

        Product product = savedRating.getProduct();
        Farmer farmer = product.getFarmer();
        if (farmer != null) {
            // Update aggregates: subtract the old score and add the new one (count remains unchanged)
            updateFarmerAggregates(farmer, savedRating.getScore() - oldScore, 0);
            updateProductAggregates(product,savedRating.getScore() - oldScore, 0);



        }

        return savedRating;
    }

    @Transactional
    public void deleteRating(int ratingId, int consumerId) throws ResourceNotFoundException {
        if (!ratingRepo.existsByConsumer_ConsumerIdAndRatingId(consumerId, ratingId)) {
            throw new ResourceNotFoundException("Rating not found with ID: " + ratingId + " for consumer ID: " + consumerId);
        }

        Rating ratingToDelete = ratingRepo.findById(ratingId)
                .orElseThrow(() -> new ResourceNotFoundException("Rating not found with ID: " + ratingId));

        Product product = ratingToDelete.getProduct();

        Farmer farmer = product.getFarmer();


        ratingRepo.deleteById(ratingId);

        if (farmer != null) {
            // Subtract the rating's score and decrement the count
            updateFarmerAggregates(farmer, -ratingToDelete.getScore(), -1);
            updateProductAggregates(product,-ratingToDelete.getScore(), -1);
        }
    }

    public Set<Rating> getConsumerRatings(int consumerId) {
        Consumer consumer = consumerRepo.findById(consumerId)
                .orElseThrow(() -> new IllegalArgumentException("Consumer not found with ID: " + consumerId));
        return consumer.getConsumerRatings();
    }

    @Transactional
    public Set<Rating> getProductRatings(int productId) {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + productId));
        return product.getRatings();
    }
}
