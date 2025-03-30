package com.example.finalyearproject.Abstraction;

import com.example.finalyearproject.DataStore.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories
public interface OrderRepo extends JpaRepository<Order,Integer> {

    @Query("select 1 from Order o where o.orderStatus=:#{#status} and o.consumer.consumerId=:#{#consumerId}")
    public Order findByStatusAndConsumerId(String status,int consumerId);
}
