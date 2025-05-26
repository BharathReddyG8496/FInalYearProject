

package com.example.finalyearproject.Utility;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsumerSummaryDTO {
    private int consumerId;
    private String consumerFirstName;
    private String consumerLastName;
    private String consumerEmail;
    private String profilePhotoPath;
}