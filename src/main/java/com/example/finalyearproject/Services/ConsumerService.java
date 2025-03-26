package com.example.finalyearproject.Services;

import com.example.finalyearproject.Abstraction.ConsumerRepo;
import com.example.finalyearproject.DataStore.Consumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class ConsumerService {

    @Autowired
    private ConsumerRepo consumerRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Consumer RegisterConsumer(Consumer consumer) {

        consumer.setConsumerPassword(passwordEncoder.encode(consumer.getConsumerPassword()));
        return consumerRepo.save(consumer);
    }

    public void UpdateConsumer(Consumer consumer){
        consumerRepo.updateConsumerByconsumerId(consumer, consumer.getConsumerId());

    }




}
