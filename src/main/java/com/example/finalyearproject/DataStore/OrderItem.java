package com.example.finalyearproject.DataStore;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int orderItemId;

    @NotNull(message = "Quantity cannot be null")
    private int quantity;

    @NotNull(message = "UnitPrice cannot be null")
    private double unitPrice;

    @ManyToOne()
    @JsonBackReference("order-items")
    private Order order;

    private String fieldChange;

    @Column(nullable = false)
    private boolean isRated = false;

    // Track fulfillment status for each item
    @Enumerated(EnumType.STRING)
    private FulfillmentStatus fulfillmentStatus = FulfillmentStatus.PENDING;

    // Track status change timestamps
    @Temporal(TemporalType.TIMESTAMP)
    private Date deliveredAt;

    @Temporal(TemporalType.TIMESTAMP)
    private Date confirmedAt;

    // Track cancellation details
    @Temporal(TemporalType.TIMESTAMP)
    private Date cancelledAt;

    // Who cancelled the item (consumer, farmer, or system)
    private String cancelledBy;

    // Reason for cancellation
    private String cancellationReason;

    // Optional notes from farmer about delivery
    private String deliveryNotes;

    @JsonProperty("productId")
    public Integer getProductId() {
        return product != null ? product.getProductId() : null;
    }

    @JsonProperty("productName")
    public String getProductName() {
        return product != null ? product.getName() : null;
    }

    @JsonProperty("farmerName")
    public String getFarmerName() {
        return product != null && product.getFarmer() != null ?
                product.getFarmer().getFarmerName() : null;
    }

    @JsonProperty("farmerId")
    public Integer getFarmerId() {
        return product != null && product.getFarmer() != null ?
                product.getFarmer().getFarmerId() : null;
    }

    @ManyToOne
    @JoinColumn(name = "product_id")
    @JsonBackReference("order-product")
    private Product product;

    // CHANGE THIS METHOD: Update to handle items cancelled after delivery
    public void cancel(String cancelledBy, String reason) {
        // Store previous status for potential logging
        FulfillmentStatus previousStatus = this.fulfillmentStatus;

        this.fulfillmentStatus = FulfillmentStatus.CANCELLED;
        this.cancelledAt = new Date();
        this.cancelledBy = cancelledBy;
        this.cancellationReason = reason;

        // Add note if cancelling after delivery
        if (previousStatus == FulfillmentStatus.DELIVERED) {
            this.cancellationReason = (reason != null && !reason.isEmpty() ? reason + " " : "") +
                    "(Cancelled after delivery)";
        }
    }

    // CHANGE THIS METHOD: Update to accept userRole parameter
    public boolean canBeCancelled(String userRole) {
        if ("CONSUMER".equals(userRole)) {
            // Consumers can cancel if not already confirmed or cancelled
            return this.fulfillmentStatus != FulfillmentStatus.CONFIRMED &&
                    this.fulfillmentStatus != FulfillmentStatus.CANCELLED;
        } else {
            // Farmers and others can only cancel pending items
            return this.fulfillmentStatus == FulfillmentStatus.PENDING;
        }
    }

    // IMPORTANT: Add this fallback method for backward compatibility
    // This ensures existing code that calls canBeCancelled without a role still works
    public boolean canBeCancelled() {
        // Default behavior: only pending items can be cancelled
        return this.fulfillmentStatus == FulfillmentStatus.PENDING;
    }
}