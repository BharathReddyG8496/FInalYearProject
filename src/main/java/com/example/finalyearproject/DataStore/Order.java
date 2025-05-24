package com.example.finalyearproject.DataStore;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int orderId;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus = OrderStatus.CREATED;

    private double totalAmount;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    @Temporal(TemporalType.TIMESTAMP)
    private Date placedAt;

    // Track when order was cancelled
    @Temporal(TemporalType.TIMESTAMP)
    private Date cancelledAt;

    // Reason for order cancellation
    private String cancellationReason;

    // Shipping address details
    private String shippingAddress;
    private String shippingCity;
    private String shippingState;
    private String shippingZip;

    @ManyToOne
    @JoinColumn(name = "consumer_id", nullable = false)
    @JsonBackReference("consumer-order")
    private Consumer consumer;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("order-items")
    private Set<OrderItem> orderItems = new HashSet<>();

    @OneToOne(mappedBy = "order")
    @JsonManagedReference("delivery-order")
    private DeliveryAddresses deliveryAddress;

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
        updatedAt = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date();
    }

    // Helper method to place the order
    public void place() {
        this.orderStatus = OrderStatus.PLACED;
        this.placedAt = new Date();

        // Initialize all items to PENDING status when order is placed
        for (OrderItem item : this.orderItems) {
            item.setFulfillmentStatus(FulfillmentStatus.PENDING);
        }
    }

    // Helper method to recalculate order total
    public void recalculateTotal() {
        // Only include non-cancelled items in total
        this.totalAmount = orderItems.stream()
                .filter(item -> item.getFulfillmentStatus() != FulfillmentStatus.CANCELLED)
                .mapToDouble(item -> item.getQuantity() * item.getUnitPrice())
                .sum();
    }

    // Cancel the entire order
    public boolean cancelOrder(String reason) {
        // Can only cancel if order is in CREATED or PLACED status
        // and no items have been delivered yet
        if (orderStatus != OrderStatus.CREATED && orderStatus != OrderStatus.PLACED) {
            return false;
        }

        boolean anyDelivered = orderItems.stream()
                .anyMatch(item -> item.getFulfillmentStatus() == FulfillmentStatus.DELIVERED ||
                        item.getFulfillmentStatus() == FulfillmentStatus.CONFIRMED);
        if (anyDelivered) {
            return false;
        }

        // Mark all items as cancelled
        for (OrderItem item : orderItems) {
            if (item.canBeCancelled()) {
                item.cancel("CONSUMER", reason);
            }
        }

        this.orderStatus = OrderStatus.CANCELLED;
        this.cancelledAt = new Date();
        this.cancellationReason = reason;

        return true;
    }

    // Update order status based on item statuses
    public void updateStatusBasedOnItems() {
        // If there are no items, do nothing
        if (orderItems.isEmpty()) {
            return;
        }

        // Check various conditions
        boolean allConfirmed = orderItems.stream()
                .allMatch(item -> item.getFulfillmentStatus() == FulfillmentStatus.CONFIRMED);

        boolean allDeliveredOrConfirmed = orderItems.stream()
                .allMatch(item -> item.getFulfillmentStatus() == FulfillmentStatus.DELIVERED ||
                        item.getFulfillmentStatus() == FulfillmentStatus.CONFIRMED);

        boolean allCancelled = orderItems.stream()
                .allMatch(item -> item.getFulfillmentStatus() == FulfillmentStatus.CANCELLED);

        // Update order status based on item statuses
        if (allCancelled) {
            if (this.orderStatus != OrderStatus.CANCELLED) {
                this.orderStatus = OrderStatus.CANCELLED;
                this.cancelledAt = new Date();
                this.cancellationReason = "All items cancelled";
            }
        } else if (allConfirmed) {
            this.orderStatus = OrderStatus.COMPLETED;
        } else if (allDeliveredOrConfirmed) {
            this.orderStatus = OrderStatus.DELIVERED;
        } else if (this.orderStatus == OrderStatus.CREATED) {
            // Don't change PLACED status back to CREATED if some items are delivered
            return;
        } else if (this.orderStatus != OrderStatus.CANCELLED) {
            this.orderStatus = OrderStatus.PLACED;
        }
    }

    // Get items for a specific farmer
    public Set<OrderItem> getItemsForFarmer(String farmerEmail) {
        return this.orderItems.stream()
                .filter(item -> item.getProduct() != null &&
                        item.getProduct().getFarmer() != null &&
                        item.getProduct().getFarmer().getFarmerEmail().equals(farmerEmail))
                .collect(Collectors.toSet());
    }

    // Check if any items in the order can still be cancelled
    public boolean canBeCancelled() {
        if (orderStatus == OrderStatus.CANCELLED ||
                orderStatus == OrderStatus.COMPLETED) {
            return false;
        }

        // Check if there are any items that can be cancelled
        return orderItems.stream()
                .anyMatch(OrderItem::canBeCancelled);
    }

    // Get active (non-cancelled) items
    public Set<OrderItem> getActiveItems() {
        return orderItems.stream()
                .filter(item -> item.getFulfillmentStatus() != FulfillmentStatus.CANCELLED)
                .collect(Collectors.toSet());
    }
}