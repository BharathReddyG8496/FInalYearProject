package com.example.finalyearproject.Abstraction;

import com.example.finalyearproject.DataStore.Donation;
import com.example.finalyearproject.DataStore.DonationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DonationRepo extends JpaRepository<Donation, Long> {

    List<Donation> findByConsumer_ConsumerIdOrderByCreatedAtDesc(int consumerId);

    List<Donation> findByFarmer_FarmerIdOrderByCreatedAtDesc(int farmerId);

    Optional<Donation> findByTransactionId(String transactionId);

    @Query("SELECT SUM(d.amount) FROM Donation d WHERE d.farmer.farmerId = :farmerId AND d.status = :status")
    Double getTotalDonationsByFarmerAndStatus(int farmerId, DonationStatus status);

    @Query("SELECT SUM(d.amount) FROM Donation d WHERE d.consumer.consumerId = :consumerId AND d.status = :status")
    Double getTotalDonationsByConsumerAndStatus(int consumerId, DonationStatus status);
}