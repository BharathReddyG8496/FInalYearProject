package com.example.finalyearproject.Services;

import com.example.finalyearproject.Abstraction.ConsumerRepo;
import com.example.finalyearproject.Abstraction.DeliveryAddressesRepo;
import com.example.finalyearproject.DataStore.Consumer;
import com.example.finalyearproject.DataStore.DeliveryAddresses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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


    public Consumer RegisterConsumer(@Valid Consumer consumer) {

        consumer.setConsumerPassword(passwordEncoder.encode(consumer.getConsumerPassword()));
        return consumerRepo.save(consumer);
    }

    public void UpdateConsumer(Consumer consumer, int id){
        consumerRepo.updateConsumerByconsumerId(consumer, id);

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
        return null;
    }

    public DeliveryAddresses UpdateDeliveryAddress(DeliveryAddresses addresses,int consumerId,int addressId){
        if(addresses!=null && consumerId!=0 && addressId!=0){
            consumerRepo.updateDeliveryAddress(addresses,addressId,consumerId);
            return deliveryAddressesRepo.findDeliveryAddressesByDeliveryAddressId(addressId);
        }else{
            return null;
        }
    }

    public Set<DeliveryAddresses> DeleteDeliveryAddress(int addressId,int consumerId){
        if(addressId!=0 && consumerId!=0){
            consumerRepo.deleteDeliveryAddressById(addressId,consumerId);
            return consumerRepo.findConsumerByConsumerId(consumerId).getSetOfDeliveryAddress();
        }
        return null;
    }




}
