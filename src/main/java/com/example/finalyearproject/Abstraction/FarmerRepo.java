package com.example.finalyearproject.Abstraction;

import com.example.finalyearproject.DataStore.Farmer;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.Optional;

@Transactional
@EnableJpaRepositories
public interface FarmerRepo extends JpaRepository<Farmer,Integer> {
     Optional<Farmer> findFarmerByFarmerEmail(String farmerName);

     Farmer findFarmerByFarmerId(int farmerId);


    @Modifying
    @Query("update Farmer set farmerName=:#{#farmer.farmerName}, farmerAddress=:#{#farmer.farmerAddress},farmerPhone=:#{#farmer.farmerPhone},firstName=:#{#farmer.firstName},lastName=:#{#farmer.lastName}, farmerEmail=:#{#farmer.farmerEmail} where farmerId=:#{#farmerId}")
    void updateByFarmerId(Farmer farmer,int farmerId);
}
