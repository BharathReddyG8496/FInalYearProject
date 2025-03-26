package com.example.finalyearproject.Services;

import com.example.finalyearproject.Abstraction.FarmerRepo;
import com.example.finalyearproject.DataStore.Farmer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class FarmerService {

    @Autowired
    private FarmerRepo farmerRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Farmer RegisterFarmer(Farmer farmer){
        farmer.setFarmerPassword(passwordEncoder.encode(farmer.getFarmerPassword()));
        return this.farmerRepo.save(farmer);
    }

}
