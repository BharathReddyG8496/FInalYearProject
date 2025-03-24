package com.example.finalyearproject.Services;

import com.example.finalyearproject.Abstraction.ConsumerRepo;
import com.example.finalyearproject.DataStore.Consumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private ConsumerRepo consumerRepo;

    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {

        Consumer user = this.consumerRepo.findConsumerByConsumerName(userName).orElseThrow(()->
                new RuntimeException("User not Found!!!!"));
        if(user==null){
            new RuntimeException("User not Found");

        }
        return user;
    }
}