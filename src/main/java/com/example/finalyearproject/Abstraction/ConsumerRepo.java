package com.example.finalyearproject.Abstraction;

import com.example.finalyearproject.DataStore.Consumer;
import com.example.finalyearproject.DataStore.DeliveryAddresses;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
@EnableJpaRepositories
public interface ConsumerRepo extends JpaRepository<Consumer, Integer> {

    public Consumer findConsumerByConsumerId(int id);

    @Query(value = "update Consumer set consumerFirstName=:#{#consumer.consumerFirstName}, consumerLastName=:#{#consumer.consumerLastName} where consumerId=:#{#id}",nativeQuery = true)
    public void updateConsumerByconsumerId(Consumer consumer,int id);

    public Optional<Consumer> findByConsumerName(String consumerName);


//    public Set<DeliveryAddresses> addDeliv
}
