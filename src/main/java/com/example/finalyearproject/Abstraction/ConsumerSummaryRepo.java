package com.example.finalyearproject.Abstraction;

import com.example.finalyearproject.DataStore.Consumer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConsumerSummaryRepo extends JpaRepository<Consumer, Integer> {

    @Query("SELECT c FROM Consumer c ORDER BY c.consumerFirstName, c.consumerLastName")
    List<Consumer> findAllConsumersForChat();

    @Query("SELECT DISTINCT c FROM Consumer c JOIN c.consumerOrder o JOIN o.orderItems oi " +
            "JOIN oi.product p JOIN p.farmer f WHERE f.farmerEmail = :farmerEmail")
    List<Consumer> findConsumersWithOrdersFromFarmer(String farmerEmail);
}