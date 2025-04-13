package com.example.finalyearproject.Abstraction;

import com.example.finalyearproject.DataStore.Order;
import com.example.finalyearproject.DataStore.OrderItem;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.query.Param;

@EnableJpaRepositories
public interface OrderItemRepo extends JpaRepository<OrderItem, Integer> {

    @Query("select o from OrderItem o where o.order.consumer.consumerId=:#{#consumerId} and o.order.orderStatus='CREATED' and o.OrderItemId=:#{#orderItemId}")
     OrderItem findOrderItemWithStatusCREATED(int consumerId,int orderItemId);

    @Modifying
    @Transactional
    @Query("delete from OrderItem o where o.OrderItemId=:#{#orderItemId}")
     void deleteByOrderItemId(int orderItemId);

}
