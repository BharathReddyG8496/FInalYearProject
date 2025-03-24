package com.example.finalyearproject.Abstraction;

import com.example.finalyearproject.DataStore.Consumer;
import com.example.finalyearproject.DataStore.DeliveryAddresses;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@EnableJpaRepositories
public interface ConsumerRepo extends JpaRepository<Consumer, Integer> {

    public Consumer findConsumerByConsumerId(int id);
    public Consumer updateConsumerByConsumerId(Consumer consumer,int id);
    public Optional<Consumer> findConsumerByConsumerName(String ConsumerName);
//    public Set<DeliveryAddresses> addDeliv
}
