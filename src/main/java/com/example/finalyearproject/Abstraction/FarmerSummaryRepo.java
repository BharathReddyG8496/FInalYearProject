package com.example.finalyearproject.Abstraction;

import com.example.finalyearproject.DataStore.Farmer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FarmerSummaryRepo extends JpaRepository<Farmer, Integer> {

    @Query("SELECT f FROM Farmer f ORDER BY f.firstName, f.lastName")
    List<Farmer> findAllFarmersForChat();

    @Query("SELECT DISTINCT f FROM Farmer f JOIN f.farmerProducts p JOIN p.orderItems oi " +
            "JOIN oi.order o JOIN o.consumer c WHERE c.consumerEmail = :consumerEmail")
    List<Farmer> findFarmersWithOrdersFromConsumer(String consumerEmail);
}
