package com.example.finalyearproject.Utility;

import lombok.Data;
import java.util.List;

@Data
public class OrderItemStatusUpdateDTO {
    private List<Integer> orderItemIds;
    private String notes;  // Optional notes (e.g., delivery notes, cancellation reason)
}
