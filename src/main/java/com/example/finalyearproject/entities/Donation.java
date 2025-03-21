package com.example.finalyearproject.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
@Entity
@Data
public class Donation {
    @Id
    @GeneratedValue
    private int DonationId;
    private int ConsumerId;
    private int FarmerId;
    private double Amount;
    private LocalDateTime DonationDate;
    private String PaymentMethod;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "ConsumerId")
    private Consumer consumer;
}
