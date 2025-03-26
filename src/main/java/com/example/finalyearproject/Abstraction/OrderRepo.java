package com.example.finalyearproject.Abstraction;

import com.example.finalyearproject.DataStore.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories
public interface OrderRepo extends JpaRepository<Order,Integer> {
}
