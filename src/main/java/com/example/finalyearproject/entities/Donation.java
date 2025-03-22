package com.example.finalyearproject.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Donation {
    @Id
    @GeneratedValue
    private int DonationId;
    private int ConsumerId;
    private int FarmerId;
    private double Amount;
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime DonationDate;
    private String PaymentMethod;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "ConsumerId")
    private Consumer consumer;
}
