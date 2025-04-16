package com.example.finalyearproject.Controllers;


import com.example.finalyearproject.DataStore.Consumer;
import com.example.finalyearproject.DataStore.DeliveryAddresses;
import com.example.finalyearproject.Model.JwtRequest;
import com.example.finalyearproject.Model.JwtResponse;
//import com.example.finalyearproject.Security.JwtHelper;
import com.example.finalyearproject.Security.JwtHelper;
import com.example.finalyearproject.Services.ConsumerService;
import com.example.finalyearproject.Utility.ConsumerUtility;
import com.example.finalyearproject.Utility.DeliveryAddressUtility;
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
@RequestMapping("/consumer")
public class ConsumerController {

    @Autowired
    private ConsumerService consumerService;

    @PostMapping("/update-consumer/{id}")
    public ResponseEntity<String> updateConsumer(@RequestBody Consumer consumer, @PathVariable int id){
        try {
            this.consumerService.UpdateConsumer(consumer, id);
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.ok("updated ");
    }

    @PostMapping("/add-address/{consumerId}")
    public ResponseEntity<Set<DeliveryAddresses>> AddAddress(@Valid @RequestBody DeliveryAddresses deliveryAddresses,
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
    public ResponseEntity<DeliveryAddressUtility> UpdateAddress(@Valid @RequestBody DeliveryAddresses address, @PathVariable("consumerId") int consumerId, @PathVariable("addressId") int addressId){
        if(address!=null && consumerId!=0 && addressId!=0){
            DeliveryAddressUtility deliveryAddresses = consumerService.UpdateDeliveryAddress(address,consumerId,addressId);
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



    @ExceptionHandler(BadCredentialsException.class)
    public String exceptionHandler() {
        return "Credentials Invalid !!";
    }



}
