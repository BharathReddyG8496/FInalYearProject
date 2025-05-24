package com.example.finalyearproject.DataStore;

public enum OrderStatus {
    CREATED,    // Cart/Draft - initial state when adding to cart
    PLACED,     // Order has been submitted but not all items delivered
    DELIVERED,  // All items have been marked as delivered by their respective farmers
    COMPLETED,  // Consumer has confirmed receipt of all items
    CANCELLED   // Order has been cancelled
}
