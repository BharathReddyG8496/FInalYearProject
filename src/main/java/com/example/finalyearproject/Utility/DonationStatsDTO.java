package com.example.finalyearproject.Utility;

import lombok.Data;

@Data
public class DonationStatsDTO {
    private Double totalAmount;
    private String userType;  // CONSUMER or FARMER
}