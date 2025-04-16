package com.example.finalyearproject.Abstraction;

import com.example.finalyearproject.DataStore.CategoryType;
import com.example.finalyearproject.DataStore.Product;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.List;
import java.util.Optional;

@EnableJpaRepositories
public interface ProductRepo extends JpaRepository<Product,Integer> {

    @Query("select p from Product p where p.productId=:#{#productId}")
    public Product findProductByProductId(int productId);

    @Modifying
    @Transactional
    @Query("update Product p set p.name=:#{#product.name}, p.description=:#{#product.description}, p.price=:#{#product.price}" +
            ", p.stock=:#{#product.stock} where p.productId=:#{#productId} and p.farmer.farmerId=:#{#farmerId}")
    public void updateProductById(Product product,int productId, int farmerId);

    @Modifying
    @Query("delete Product where productId=:#{#productId} and farmer.farmerId=:#{#farmerId}")
    public void deleteByProductId(int productId,int farmerId);

    List<Product> findByCategory(CategoryType category);

    Optional<Product> findByFarmer_FarmerIdAndProductId(int farmerId, int productId);
}
