package com.example.finalyearproject.Abstraction;

import com.example.finalyearproject.DataStore.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories
public interface ProductRepo extends JpaRepository<Product,Integer> {

    @Query("select p from Product p where p.ProductId=:#{#productId}")
    public Product findProductByProductId(int productId);

    @Query("update Product set Name=:#{#product.name}, Description=:#{#product.description}, Price=:#{#product.price}" +
            ", Stock=:#{#product.stock} where ProductId=:#{#product.productId} and farmer.farmerId=:#{#farmerId}")
    public void updateProductById(Product product, int farmerId);

    @Query("delete Product where ProductId=:#{#productId} and farmer.farmerId=:#{#farmerId}")
    public void deleteByProductId(int productId,int farmerId);
}
