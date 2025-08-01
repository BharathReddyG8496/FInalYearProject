package com.example.finalyearproject.Utility;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;


@Data
public class OrderPlacementDTO {
    @NotBlank(message = "Shipping address is required")
    @Size(max = 255, message = "Address cannot exceed 255 characters")
    private String shippingAddress;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City cannot exceed 100 characters")
    private String shippingCity;

    @NotBlank(message = "State is required")
    @Size(max = 100, message = "State cannot exceed 100 characters")
    private String shippingState;

    @NotBlank(message = "Pincode is required")
    @Pattern(regexp = "^[0-9]{6}$", message = "Invalid pincode format (must be a 6-digit number)")
    private String shippingZip;
}