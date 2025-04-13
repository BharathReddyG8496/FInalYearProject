package com.example.finalyearproject.Abstraction;

import com.example.finalyearproject.DataStore.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Integer> {
    // You may add custom query methods later if needed.
}
