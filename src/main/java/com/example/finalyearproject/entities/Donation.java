package com.example.finalyearproject.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class Donation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int DonationId;

    @NotNull(message = "Consumer ID cannot be null")
    private int ConsumerId;

    @NotNull(message = "Farmer ID cannot be null")
    private int FarmerId;

    @NotNull(message = "Donation amount cannot be null")
    @Positive(message = "Donation amount must be positive")
    private double Amount;

    @NotNull(message = "Donation date cannot be null")
    @PastOrPresent(message = "Donation date must be in the past or present")
    private LocalDateTime DonationDate;

    @NotBlank(message = "Payment method cannot be blank")
    @Size(max = 50, message = "Payment method cannot exceed 50 characters")
    private String PaymentMethod;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "ConsumerId", insertable = false, updatable = false)
    private Consumer consumer;
}
