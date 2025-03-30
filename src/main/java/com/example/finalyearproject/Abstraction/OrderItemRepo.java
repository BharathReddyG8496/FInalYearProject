package com.example.finalyearproject.Abstraction;

import com.example.finalyearproject.DataStore.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories
public interface OrderItemRepo extends JpaRepository<OrderItem, Integer> {

}
