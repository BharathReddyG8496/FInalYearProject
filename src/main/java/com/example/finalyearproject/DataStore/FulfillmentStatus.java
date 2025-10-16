package com.example.finalyearproject.DataStore;

public enum FulfillmentStatus {
    PENDING,     // Initial state when order is placed

    DELIVERED,   // Marked by farmer when product is delivered
    CONFIRMED,   // Marked by consumer when delivery is confirmed
    CANCELLED    // Item has been cancelled (by either consumer or farmer)
}