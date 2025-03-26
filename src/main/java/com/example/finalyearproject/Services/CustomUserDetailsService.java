//package com.example.finalyearproject.Services;
//
//import com.example.finalyearproject.Abstraction.ConsumerRepo;
//import com.example.finalyearproject.DataStore.Consumer;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.stereotype.Service;
//
//import java.util.ArrayList;
//
//@Service
//public class CustomUserDetailsService implements UserDetailsService {
//
//    @Autowired
//    private ConsumerRepo consumerRepo;
//
//    @Override
//    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
//
//        Consumer user = this.consumerRepo.findByConsumerName(userName).orElseThrow(()->
//                new RuntimeException("User not Found!!!!"));
//        if(user==null){
//            throw new RuntimeException("User not Found");
//
//        }
//
//        return new org.springframework.security.core.userdetails.User(
//                user.getConsumerName(),
//                user.getPassword(),
//                new ArrayList<>()
//        );
//    }
//}