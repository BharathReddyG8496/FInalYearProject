package com.example.finalyearproject.Abstraction;

import com.example.finalyearproject.DataStore.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories
public interface OrderRepo extends JpaRepository<Order,Integer> {

    @Query("select o from Order o where o.orderStatus=:#{#status} and o.consumer.consumerId=:#{#consumerId}")
     Order findByStatusAndConsumerId(String status,int consumerId);

    @Query("select o from Order o where o.consumer.consumerId=:#{#consumerId} and o.orderStatus='CREATED'")
     Order getConsumersCart(int consumerId);

}
