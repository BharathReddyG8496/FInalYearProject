package com.example.finalyearproject.Abstraction;

import com.example.finalyearproject.DataStore.Farmer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.Optional;

@EnableJpaRepositories
public interface FarmerRepo extends JpaRepository<Farmer,Integer> {
    public Optional<Farmer> findFarmerByFarmerEmail(String farmerName);
}
