package com.example.finalyearproject.Utility;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DonationRequestDTO {
    @NotNull(message = "Farmer ID is required")
    private Integer farmerId;

    @NotNull(message = "Amount is required")
    @Min(value = 1, message = "Minimum donation amount is 1")
    private Double amount;

    private String message;  // Optional

    @NotNull(message = "Payment method is required")
    private String paymentMethod;  // UPI, CARD, etc.
}
