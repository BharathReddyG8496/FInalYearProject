package com.example.finalyearproject.Utility;

import lombok.Data;

@Data
public class DonationEligibilityDTO {
    private int farmerId;
    private boolean eligible;
    private String reason;
}
