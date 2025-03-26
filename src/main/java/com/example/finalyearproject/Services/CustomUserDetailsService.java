package com.example.finalyearproject.Services;

import com.example.finalyearproject.Abstraction.ConsumerRepo;
import com.example.finalyearproject.Abstraction.FarmerRepo;
import com.example.finalyearproject.DataStore.Consumer;
import com.example.finalyearproject.DataStore.Farmer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private ConsumerRepo consumerRepo;

    @Autowired
    private FarmerRepo farmerRepo;

    @Override
    public UserDetails loadUserByUsername(String userEmail) throws UsernameNotFoundException {

        String userEmailDto = "", userPasswordDto = "";
        try{
            Consumer consumer = this.consumerRepo.findConsumerByConsumerEmail(userEmail).orElseThrow();
            userEmailDto = consumer.getConsumerEmail();
            userPasswordDto = consumer.getPassword();
        }catch (Exception e){
            Farmer farmer = this.farmerRepo.findFarmerByFarmerEmail(userEmail).orElseThrow(() ->
                    new RuntimeException("No User found!!"));
            userEmailDto = farmer.getFarmerEmail();
            userPasswordDto = farmer.getPassword();

        }

        return new org.springframework.security.core.userdetails.User(
                userEmailDto,
                userPasswordDto,
                new ArrayList<>()
        );
    }
}