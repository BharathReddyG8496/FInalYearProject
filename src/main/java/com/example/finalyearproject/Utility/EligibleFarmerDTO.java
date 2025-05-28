package com.example.finalyearproject.Utility;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EligibleFarmerDTO {
    private int farmerId;
    private String farmerName;
    private String farmerEmail;
    private Double farmerRating;
    private int completedOrdersCount;
    private Double totalPurchaseAmount;
}