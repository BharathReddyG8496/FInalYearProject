package com.example.finalyearproject.Utility;

import com.example.finalyearproject.DataStore.DonationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DonationResponseDTO {
    private Long donationId;
    private String consumerName;
    private String farmerName;
    private Double amount;
    private String message;
    private DonationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private String transactionId;
    private String paymentUrl;  // For redirecting to payment gateway
}