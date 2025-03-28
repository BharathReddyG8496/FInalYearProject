package com.example.finalyearproject.Abstraction;

import com.example.finalyearproject.DataStore.Farmer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.Optional;

@EnableJpaRepositories
public interface FarmerRepo extends JpaRepository<Farmer,Integer> {
    public Optional<Farmer> findFarmerByFarmerEmail(String farmerName);

    public Farmer findFarmerByFarmerId(int farmerId);

    @Query("update Farmer set farmerName=:#{#farmer.farmerName}, farmerAddress=:#{#farmer.farmerAddress}," +
            " farmerPhone=:#{#farmer.farmerPhone} where farmerId=:#{#farmerId}")
    public void updateByFarmerId(Farmer farmer,int farmerId);
}
