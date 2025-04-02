package com.example.finalyearproject.Services;

import com.example.finalyearproject.Abstraction.ConsumerRepo;
import com.example.finalyearproject.Abstraction.DeliveryAddressesRepo;
import com.example.finalyearproject.DataStore.Consumer;
import com.example.finalyearproject.DataStore.DeliveryAddresses;
import com.example.finalyearproject.Utility.ConsumerUtility;
import com.example.finalyearproject.Utility.DeliveryAddressUtility;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Service
public class ConsumerService {

    @Autowired
    private ConsumerRepo consumerRepo;

    @Autowired
    private DeliveryAddressesRepo deliveryAddressesRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;


    public ConsumerUtility RegisterConsumer(@Valid Consumer consumer) {

        try {
            consumer.setConsumerPassword(passwordEncoder.encode(consumer.getConsumerPassword()));
            Consumer saved = consumerRepo.save(consumer);
            return new ConsumerUtility(200,"Created",saved);
        } catch (Exception e) {
            return new ConsumerUtility(400,e.getMessage(),null);
        }
    }

    public void UpdateConsumer(Consumer consumer, int id){
        try {
            consumerRepo.updateConsumerByconsumerId(consumer, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public Set<DeliveryAddresses> AddDeliveryAddress(DeliveryAddresses deliveryAddresses,int consumerId){
        if(deliveryAddresses!=null && consumerId!=0){
            Consumer consumer = consumerRepo.findConsumerByConsumerId(consumerId);
            deliveryAddresses.setConsumer(consumer);
            if (consumer.getSetOfDeliveryAddress() == null) {
                consumer.setSetOfDeliveryAddress(new HashSet<>());
            }
            consumer.getSetOfDeliveryAddress().add(deliveryAddresses);
            consumerRepo.save(consumer);
            return consumer.getSetOfDeliveryAddress();
        }
        return Collections.emptySet();
    }

    public DeliveryAddressUtility UpdateDeliveryAddress(DeliveryAddresses addresses,int consumerId,int addressId){
        if(addresses!=null && consumerId!=0 && addressId!=0){
            consumerRepo.updateDeliveryAddress(addresses,addressId,consumerId);
            DeliveryAddresses deliveryaddre = deliveryAddressesRepo.findDeliveryAddressesByDeliveryAddressId(addressId);
            return new DeliveryAddressUtility(200,"Updated",deliveryaddre);
        }
        return new DeliveryAddressUtility(400,"Failed to update",null);
    }

    public Set<DeliveryAddresses> DeleteDeliveryAddress(int addressId,int consumerId){
        if(addressId!=0 && consumerId!=0){
            consumerRepo.deleteDeliveryAddressById(addressId,consumerId);
            return consumerRepo.findConsumerByConsumerId(consumerId).getSetOfDeliveryAddress();
        }
        return Collections.emptySet();
    }




}
