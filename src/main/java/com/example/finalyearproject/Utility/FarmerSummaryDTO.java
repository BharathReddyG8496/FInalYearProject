package com.example.finalyearproject.Utility;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FarmerSummaryDTO {
    private int farmerId;
    private String farmerName;
    private String farmerEmail;
    private String profilePhotoPath;
}
