package com.example.finalyearproject.Controllers;


import com.example.finalyearproject.DataStore.Consumer;
import com.example.finalyearproject.DataStore.DeliveryAddresses;
import com.example.finalyearproject.Model.JwtRequest;
import com.example.finalyearproject.Model.JwtResponse;
//import com.example.finalyearproject.Security.JwtHelper;
import com.example.finalyearproject.Security.JwtHelper;
import com.example.finalyearproject.Services.ConsumerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

//
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.BadCredentialsException;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/auth")
public class ConsumerController {

    @Autowired
    private ConsumerService consumerService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtHelper jwtHelper;

    @Autowired
    private AuthenticationManager manager;


    @PostMapping(path = "/create-consumer",consumes = "application/json")
    public ResponseEntity<Consumer> RegisterConsumer(@RequestBody @Valid Consumer consumer){
        System.out.println("consumer"+consumer.getConsumerFirstName());
        Consumer consumer1 = this.consumerService.RegisterConsumer(consumer);
        if(consumer1!=null)
            return ResponseEntity.ok(consumer1);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @RequestMapping(value = "/login-consumer",method = {RequestMethod.POST,RequestMethod.GET})
    public ResponseEntity<JwtResponse> login(@RequestBody JwtRequest request) {

        this.doAuthenticate(request.getUserEmail(), request.getUserPassword());


        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUserEmail());
        String token = this.jwtHelper.generateToken(userDetails);

        JwtResponse response = JwtResponse.builder()
                .jwtToken(token)
                .userName(userDetails.getUsername()).build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/update-consumer")
    public ResponseEntity<String> updateConsumer(@RequestBody Consumer consumer){
        try {
            this.consumerService.UpdateConsumer(consumer);
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.ok("updated ");
    }

    @PostMapping("/add-address/{consumerId}")
    public ResponseEntity<Set<DeliveryAddresses>> AddAddress(@RequestBody DeliveryAddresses deliveryAddresses,
                                                             @PathVariable("consumerId")int consumerId){
        if(deliveryAddresses!=null && consumerId!=0){

            Set<DeliveryAddresses> addressesSet = consumerService.AddDeliveryAddress(deliveryAddresses,consumerId);
            if(addressesSet==null)
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            return ResponseEntity.ok(addressesSet);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @PutMapping("/update-address/{consumerId}/{addressId}")
    public ResponseEntity<DeliveryAddresses> UpdateAddress( @Valid @RequestBody DeliveryAddresses address,@PathVariable("consumerId") int consumerId,@PathVariable("addressId") int addressId){
        if(address!=null && consumerId!=0 && addressId!=0){
            DeliveryAddresses deliveryAddresses = consumerService.UpdateDeliveryAddress(address,consumerId,addressId);
            if(deliveryAddresses!=null)
                return ResponseEntity.ok(deliveryAddresses);

        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @DeleteMapping("/delete-address/{consumerId}/{addressId}")
    public ResponseEntity<Set<DeliveryAddresses>> DeleteAddresses(@PathVariable("consumerId") int consumerId,@PathVariable("addressId")int addressId){
        if(consumerId!=0 && addressId!=0){
            Set<DeliveryAddresses> deliveryAddresses = consumerService.DeleteDeliveryAddress(addressId,consumerId);
            if(deliveryAddresses!=null){
                return ResponseEntity.ok(deliveryAddresses);
            }
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    private void doAuthenticate(String userName, String password) {

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userName, password);
        try {
            manager.authenticate(authentication);


        } catch (BadCredentialsException e) {
            throw new BadCredentialsException(" Invalid Admin_name or Password  !!");
        }

    }

    @ExceptionHandler(BadCredentialsException.class)
    public String exceptionHandler() {
        return "Credentials Invalid !!";
    }



}
