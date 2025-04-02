package com.example.finalyearproject.Abstraction;

import com.example.finalyearproject.DataStore.Rating;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Transactional
public interface RatingRepo extends JpaRepository<Rating,Integer> {


    List<Rating> deleteByRatingId(int RatingId);

    boolean existsByConsumer_ConsumerIdAndRatingId(int consumerId, int RatingId);

}
