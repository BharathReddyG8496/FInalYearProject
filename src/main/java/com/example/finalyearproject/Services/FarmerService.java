package com.example.finalyearproject.Services;

import com.example.finalyearproject.Abstraction.FarmerRepo;
import com.example.finalyearproject.DataStore.Farmer;
import com.example.finalyearproject.Utility.FarmerUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class FarmerService {

    @Autowired
    private FarmerRepo farmerRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public FarmerUtility RegisterFarmer(Farmer farmer){
        try {
            farmer.setFarmerPassword(passwordEncoder.encode(farmer.getFarmerPassword()));
            Farmer save = this.farmerRepo.save(farmer);
            return new FarmerUtility(200,"Registered",save);
        } catch (Exception e) {
            return new FarmerUtility(400,"failed to register",null);
        }

    }

    public Optional<Farmer> UpdateFarmer(Farmer farmer, int farmerId){
        farmerRepo.updateByFarmerId(farmer,farmerId);
         Optional<Farmer> save=this.farmerRepo.findByFarmerId(farmerId);
         return save;
    }


}
