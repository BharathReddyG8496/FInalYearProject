package com.example.finalyearproject.Utility;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsumerUpdateDTO {
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String consumerFirstName;

    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String consumerLastName;

    @Pattern(regexp = "^(\\+91|0)?\\d{10}$", message = "Invalid phone number format")
    private String consumerPhone;

    @Size(max = 255, message = "Address cannot exceed 255 characters")
    private String consumerAddress;

    // Optional password field - only update if provided
//    private String password;
}
