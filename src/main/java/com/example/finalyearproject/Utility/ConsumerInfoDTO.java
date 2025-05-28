package com.example.finalyearproject.Utility;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsumerInfoDTO {
    private int consumerId;
    private String consumerName;
    private String consumerEmail;
    private String consumerPhone;
}