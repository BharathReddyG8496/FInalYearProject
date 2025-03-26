package com.example.finalyearproject.Abstraction;

import com.example.finalyearproject.DataStore.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories
public interface ProductRepo extends JpaRepository<Product,Integer> {

}
