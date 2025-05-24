package com.example.finalyearproject.Services;

import com.example.finalyearproject.Abstraction.*;
import com.example.finalyearproject.DataStore.*;
import com.example.finalyearproject.Utility.ApiResponse;
import com.example.finalyearproject.Utility.OrderPlacementDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


@Service
public class OrderService {
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private OrderRepo orderRepo;

    @Autowired
    private OrderItemRepo orderItemRepo;

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private FarmerRepo farmerRepo;

    @Autowired
    private ConsumerRepo consumerRepo;

    @Autowired
    private DeliveryAddressesRepo deliveryAddressesRepo;

    /**
     * Place order and reduce product stock
     * Uses higher isolation level to prevent overselling
     */
    /**
     * Place order and reduce product stock
     * Uses higher isolation level to prevent overselling
     * Accounts for both stock changes and price changes
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public ApiResponse<Order> placeOrder(int consumerId, OrderPlacementDTO placementDTO) {
        try {
            // Get active cart
            Order cart = orderRepo.findActiveCartByConsumerId(consumerId);
            if (cart == null || cart.getOrderItems().isEmpty()) {
                return ApiResponse.error("Empty cart", "Your cart is empty");
            }

            // Check for stock and price changes
            StringBuilder stockErrors = new StringBuilder();
            StringBuilder priceChangeNotes = new StringBuilder();
            boolean hasStockError = false;
            boolean hasPriceChanges = false;
            double originalTotal = cart.getTotalAmount();
            double newTotal = 0;

            for (OrderItem item : cart.getOrderItems()) {
                // Reload product to get latest data
                Product product = productRepo.findById(item.getProduct().getProductId()).orElse(null);
                if (product == null) {
                    stockErrors.append("Product ").append(item.getProduct().getName())
                            .append(" is no longer available. ");
                    hasStockError = true;
                    continue;
                }

                // Check stock availability
                if (product.getStock() < item.getQuantity()) {
                    stockErrors.append("Only ").append(product.getStock())
                            .append(" units available for ").append(product.getName())
                            .append(" (requested: ").append(item.getQuantity()).append("). ");
                    hasStockError = true;
                    continue;
                }

                // Check for price changes
                double currentUnitPrice = product.getPrice();
                double cartUnitPrice = item.getUnitPrice() / item.getQuantity();

                if (Math.abs(currentUnitPrice - cartUnitPrice) > 0.01) { // Using a small epsilon for double comparison
                    // Price has changed
                    hasPriceChanges = true;
                    priceChangeNotes.append(product.getName())
                            .append(": Price changed from $").append(String.format("%.2f", cartUnitPrice))
                            .append(" to $").append(String.format("%.2f", currentUnitPrice))
                            .append(" per unit. ");

                    // Update the price in the cart item
                    double newItemTotal = currentUnitPrice * item.getQuantity();
                    item.setUnitPrice(newItemTotal);
                    item.setFieldChange("Price updated from $" + String.format("%.2f", cartUnitPrice) +
                            " to $" + String.format("%.2f", currentUnitPrice) + " per unit");
                }

                // Add to new total
                newTotal += item.getUnitPrice();
            }

            // Return error if stock issues
            if (hasStockError) {
                return ApiResponse.error("Insufficient stock", stockErrors.toString());
            }

            // Update cart total with new prices
            if (hasPriceChanges) {
                cart.setTotalAmount(newTotal);
                orderItemRepo.saveAll(cart.getOrderItems()); // Save updated prices

                // Return a warning about price changes
                return ApiResponse.error("Price changes detected",
                        "Some prices have changed since items were added to your cart. " +
                                "Please review your cart and confirm: " + priceChangeNotes.toString());
            }

            // Set shipping information
            if (placementDTO != null) {
                cart.setShippingAddress(placementDTO.getShippingAddress());
                cart.setShippingCity(placementDTO.getShippingCity());
                cart.setShippingState(placementDTO.getShippingState());
                cart.setShippingZip(placementDTO.getShippingZip());

                // Create delivery address from shipping info
                if (cart.getDeliveryAddress() == null) {
                    DeliveryAddresses deliveryAddress = new DeliveryAddresses();
                    deliveryAddress.setStreetAddress(placementDTO.getShippingAddress());
                    deliveryAddress.setCity(placementDTO.getShippingCity());
                    deliveryAddress.setState(placementDTO.getShippingState());
                    deliveryAddress.setPincode(placementDTO.getShippingZip());

                    // Use existing consumer object from cart
                    deliveryAddress.setConsumer(cart.getConsumer());

                    // Set bidirectional relationship
                    deliveryAddress.setOrder(cart);
                    cart.setDeliveryAddress(deliveryAddress);

                    // Save explicitly
                    deliveryAddressesRepo.save(deliveryAddress);
                }
            }

            // Update stock for each product
            for (OrderItem item : cart.getOrderItems()) {
                Product product = item.getProduct();
                int newStock = product.getStock() - item.getQuantity();
                product.setStock(newStock);
                productRepo.save(product);

                // Initialize item fulfillment status
                item.setFulfillmentStatus(FulfillmentStatus.PENDING);
            }

            // Update order status
            cart.place(); // Sets status to PLACED and timestamps
            Order placedOrder = orderRepo.save(cart);

            return ApiResponse.success("Order placed successfully", placedOrder);
        } catch (Exception e) {
            logger.error("Failed to place order: {}", e.getMessage(), e);
            return ApiResponse.error("Failed to place order", e.getMessage());
        }
    }
    /**
     * Get order history for a consumer
     */
    public List<Order> getOrderHistory(int consumerId) {
        return orderRepo.findByConsumer_ConsumerIdOrderByCreatedAtDesc(consumerId);
    }

    /**
     * Get order history for a consumer by email
     * OPTIMIZATION: Using previously unused repository method
     */
    public List<Order> getOrderHistoryByEmail(String consumerEmail) {
        return orderRepo.findByConsumer_ConsumerEmailOrderByCreatedAtDesc(consumerEmail);
    }

    /**
     * Get order by ID
     */
    public Order getOrderById(int orderId) {
        return orderRepo.findById(orderId).orElse(null);
    }

    /**
     * Mark specific items from a farmer as delivered
     * OPTIMIZATION: Using findByOrderIdAndFarmerEmail instead of in-memory filtering
     */
    @Transactional
    public ApiResponse<List<OrderItem>> markFarmerItemsDelivered(int orderId, String farmerEmail, String deliveryNotes) {
        try {
            // Find order
            Order order = orderRepo.findById(orderId).orElse(null);
            if (order == null) {
                return ApiResponse.error("Order not found", "No order found with ID: " + orderId);
            }

            // Only allow updating items in PLACED orders
            if (order.getOrderStatus() != OrderStatus.PLACED) {
                return ApiResponse.error("Invalid order status",
                        "Can only mark items as delivered for orders in PLACED status. Current status: " + order.getOrderStatus());
            }

            // Find farmer's items in this order using database query instead of in-memory filtering
            Set<OrderItem> farmerItems = orderItemRepo.findByOrderIdAndFarmerEmail(orderId, farmerEmail);
            if (farmerItems.isEmpty()) {
                return ApiResponse.error("No items found", "No items from this farmer in order #" + orderId);
            }

            // Mark farmer's pending items as delivered
            List<OrderItem> updatedItems = new ArrayList<>();
            for (OrderItem item : farmerItems) {
                if (item.getFulfillmentStatus() == FulfillmentStatus.PENDING) {
                    item.setFulfillmentStatus(FulfillmentStatus.DELIVERED);
                    item.setDeliveredAt(new Date());
                    if (deliveryNotes != null && !deliveryNotes.isEmpty()) {
                        item.setDeliveryNotes(deliveryNotes);
                    }
                    orderItemRepo.save(item);
                    updatedItems.add(item);
                }
            }

            if (updatedItems.isEmpty()) {
                return ApiResponse.error("No items updated",
                        "All items from this farmer are already delivered or cancelled");
            }

            // Update order status based on item statuses
            order.updateStatusBasedOnItems();
            orderRepo.save(order);

            return ApiResponse.success("Items marked as delivered", updatedItems);
        } catch (Exception e) {
            logger.error("Failed to mark items as delivered: {}", e.getMessage(), e);
            return ApiResponse.error("Update failed", e.getMessage());
        }
    }

    /**
     * Consumer confirms receipt of specific items
     * OPTIMIZATION: Using findByOrderIdAndItemIds instead of in-memory filtering
     */
    @Transactional
    public ApiResponse<List<OrderItem>> confirmItemsReceipt(int orderId, List<Integer> orderItemIds, String consumerEmail) {
        try {
            // Find order and verify ownership
            Order order = orderRepo.findById(orderId).orElse(null);
            if (order == null) {
                return ApiResponse.error("Order not found", "No order found with ID: " + orderId);
            }

            // Verify order belongs to this consumer
            if (!order.getConsumer().getConsumerEmail().equalsIgnoreCase(consumerEmail)) {
                return ApiResponse.error("Access denied", "You don't have permission to update this order");
            }

            // Find the specified items in this order using direct database query
            List<OrderItem> itemsToConfirm = orderItemRepo.findByOrderIdAndItemIds(orderId, orderItemIds);

            if (itemsToConfirm.isEmpty()) {
                return ApiResponse.error("No items found", "No matching items found in order #" + orderId);
            }

            // Confirm delivered items
            List<OrderItem> confirmedItems = new ArrayList<>();
            for (OrderItem item : itemsToConfirm) {
                if (item.getFulfillmentStatus() == FulfillmentStatus.DELIVERED) {
                    item.setFulfillmentStatus(FulfillmentStatus.CONFIRMED);
                    item.setConfirmedAt(new Date());
                    orderItemRepo.save(item);
                    confirmedItems.add(item);
                }
            }

            if (confirmedItems.isEmpty()) {
                return ApiResponse.error("No items confirmed",
                        "Items must be in DELIVERED status to confirm receipt");
            }

            // Update order status based on item statuses
            order.updateStatusBasedOnItems();
            orderRepo.save(order);

            return ApiResponse.success("Items confirmed", confirmedItems);
        } catch (Exception e) {
            logger.error("Failed to confirm items receipt: {}", e.getMessage(), e);
            return ApiResponse.error("Update failed", e.getMessage());
        }
    }

    /**
     * Cancel specific items in an order
     * OPTIMIZATION: Using findByOrderIdAndItemIds for targeted item retrieval
     */
    @Transactional
    public ApiResponse<List<OrderItem>> cancelOrderItems(int orderId, List<Integer> orderItemIds,
                                                         String userEmail, String userRole, String reason) {
        try {
            // Find order
            Order order = orderRepo.findById(orderId).orElse(null);
            if (order == null) {
                return ApiResponse.error("Order not found", "No order found with ID: " + orderId);
            }

            // Verify ownership based on role
            if ("CONSUMER".equals(userRole)) {
                if (!order.getConsumer().getConsumerEmail().equalsIgnoreCase(userEmail)) {
                    return ApiResponse.error("Access denied", "You don't have permission to update this order");
                }
            } else if ("FARMER".equals(userRole)) {
                // Farmers can only cancel their own items
                boolean hasAccess = orderItemRepo.findByFarmerEmail(userEmail).stream()
                        .anyMatch(item -> item.getOrder().getOrderId() == orderId);

                if (!hasAccess) {
                    return ApiResponse.error("Access denied", "No products from your farm in this order");
                }
            }

            // Find the specified items using database query
            List<OrderItem> allTargetItems = orderItemRepo.findByOrderIdAndItemIds(orderId, orderItemIds);

            // Filter for items this user can cancel
            List<OrderItem> itemsToCancel = new ArrayList<>();
            for (OrderItem item : allTargetItems) {
                if ("FARMER".equals(userRole)) {
                    // Verify item belongs to this farmer
                    if (item.getProduct() != null &&
                            item.getProduct().getFarmer() != null &&
                            item.getProduct().getFarmer().getFarmerEmail().equalsIgnoreCase(userEmail)) {
                        itemsToCancel.add(item);
                    }
                } else {
                    // Consumers can cancel any item in their order
                    itemsToCancel.add(item);
                }
            }

            if (itemsToCancel.isEmpty()) {
                return ApiResponse.error("No items found", "No matching items found that you can cancel");
            }

            // Cancel eligible items - THIS IS THE IMPORTANT CHANGE
            List<OrderItem> cancelledItems = new ArrayList<>();
            for (OrderItem item : itemsToCancel) {
                // Pass the user role to canBeCancelled
                if (item.canBeCancelled(userRole)) {
                    // Restore product stock
                    Product product = item.getProduct();
                    if (product != null) {
                        product.setStock(product.getStock() + item.getQuantity());
                        productRepo.save(product);
                    }

                    // Cancel the item
                    item.cancel(userRole, reason);
                    orderItemRepo.save(item);
                    cancelledItems.add(item);
                }
            }

            if (cancelledItems.isEmpty()) {
                if ("CONSUMER".equals(userRole)) {
                    return ApiResponse.error("No items cancelled",
                            "Items cannot be cancelled (already confirmed)");
                } else {
                    return ApiResponse.error("No items cancelled",
                            "Items cannot be cancelled (already delivered or confirmed)");
                }
            }

            // Recalculate total and update order status
            order.recalculateTotal();
            order.updateStatusBasedOnItems();
            orderRepo.save(order);

            return ApiResponse.success("Items cancelled", cancelledItems);
        } catch (Exception e) {
            logger.error("Failed to cancel items: {}", e.getMessage(), e);
            return ApiResponse.error("Cancellation failed", e.getMessage());
        }
    }

    /**
     * Cancel entire order
     */
    @Transactional
    public ApiResponse<Order> cancelOrder(int orderId, String consumerEmail, String reason) {
        try {
            // Find order
            Order order = orderRepo.findById(orderId).orElse(null);
            if (order == null) {
                return ApiResponse.error("Order not found", "No order found with ID: " + orderId);
            }

            // Verify order belongs to this consumer
            if (!order.getConsumer().getConsumerEmail().equalsIgnoreCase(consumerEmail)) {
                return ApiResponse.error("Access denied", "You don't have permission to cancel this order");
            }

            // Try to cancel the order
            boolean cancelled = order.cancelOrder(reason);
            if (!cancelled) {
                return ApiResponse.error("Cannot cancel",
                        "Order cannot be cancelled (already delivered or in final status)");
            }

            // Restore stock for all cancelled items
            for (OrderItem item : order.getOrderItems()) {
                if (item.getFulfillmentStatus() == FulfillmentStatus.CANCELLED) {
                    Product product = item.getProduct();
                    if (product != null) {
                        product.setStock(product.getStock() + item.getQuantity());
                        productRepo.save(product);
                    }
                }
            }

            orderRepo.save(order);
            return ApiResponse.success("Order cancelled", order);
        } catch (Exception e) {
            logger.error("Failed to cancel order: {}", e.getMessage(), e);
            return ApiResponse.error("Cancellation failed", e.getMessage());
        }
    }

    /**
     * Get all orders containing products from a specific farmer with PLACED status
     */
    public ApiResponse<List<Order>> getFarmerOrders(String farmerEmail) {
        try {
            // Verify farmer exists
            Farmer farmer = farmerRepo.findByFarmerEmail(farmerEmail);
            if (farmer == null) {
                return ApiResponse.error("Failed to retrieve orders", "Farmer not found");
            }

            List<Order> orders = orderRepo.findPlacedOrdersContainingFarmerProducts(farmerEmail);
            return ApiResponse.success("Farmer orders retrieved successfully", orders);
        } catch (Exception e) {
            logger.error("Failed to retrieve farmer orders: {}", e.getMessage(), e);
            return ApiResponse.error("Failed to retrieve orders", e.getMessage());
        }
    }

    /**
     * Get all orders with at least one item in PENDING status
     * OPTIMIZATION: Using previously unused findOrdersWithPendingItems
     */
    public ApiResponse<List<Order>> getOrdersWithPendingItems() {
        try {
            List<Order> orders = orderRepo.findOrdersWithPendingItems();
            return ApiResponse.success("Orders with pending items retrieved", orders);
        } catch (Exception e) {
            logger.error("Failed to retrieve orders with pending items: {}", e.getMessage(), e);
            return ApiResponse.error("Failed to retrieve orders", e.getMessage());
        }
    }

    /**
     * Get details for a specific order with only this farmer's products
     * OPTIMIZATION: Using findByOrderIdAndFarmerEmail for efficient item filtering
     */
    public ApiResponse<Order> getFarmerOrderDetails(int orderId, String farmerEmail) {
        try {
            Order order = orderRepo.findById(orderId).orElse(null);
            if (order == null) {
                return ApiResponse.error("Order not found", "No order found with ID: " + orderId);
            }

            // Get only this farmer's items using optimized query
            Set<OrderItem> farmerItems = orderItemRepo.findByOrderIdAndFarmerEmail(orderId, farmerEmail);

            if (farmerItems.isEmpty()) {
                return ApiResponse.error("Access denied", "This order does not contain your products");
            }

            // Create a filtered view of the order with only this farmer's items
            Order filteredOrder = new Order();
            filteredOrder.setOrderId(order.getOrderId());
            filteredOrder.setOrderStatus(order.getOrderStatus());
            filteredOrder.setTotalAmount(farmerItems.stream()
                    .mapToDouble(item -> item.getQuantity() * item.getUnitPrice())
                    .sum());
            filteredOrder.setCreatedAt(order.getCreatedAt());
            filteredOrder.setUpdatedAt(order.getUpdatedAt());
            filteredOrder.setPlacedAt(order.getPlacedAt());
            filteredOrder.setShippingAddress(order.getShippingAddress());
            filteredOrder.setShippingCity(order.getShippingCity());
            filteredOrder.setShippingState(order.getShippingState());
            filteredOrder.setShippingZip(order.getShippingZip());
            filteredOrder.setConsumer(order.getConsumer());
            filteredOrder.setOrderItems(farmerItems);

            return ApiResponse.success("Order details retrieved", filteredOrder);
        } catch (Exception e) {
            logger.error("Failed to retrieve order details: {}", e.getMessage(), e);
            return ApiResponse.error("Failed to retrieve order details", e.getMessage());
        }
    }

    /**
     * Get all orders for a farmer regardless of status
     * OPTIMIZATION: Using previously unused findAllOrdersContainingFarmerProducts
     */
    public ApiResponse<List<Order>> getFarmerAllOrders(String farmerEmail) {
        try {
            // Verify farmer exists
            Farmer farmer = farmerRepo.findByFarmerEmail(farmerEmail);
            if (farmer == null) {
                return ApiResponse.error("Failed to retrieve orders", "Farmer not found");
            }

            List<Order> orders = orderRepo.findAllOrdersContainingFarmerProducts(farmerEmail);
            return ApiResponse.success("All farmer orders retrieved successfully", orders);
        } catch (Exception e) {
            logger.error("Failed to retrieve all farmer orders: {}", e.getMessage(), e);
            return ApiResponse.error("Failed to retrieve orders", e.getMessage());
        }
    }

    /**
     * Get all items sold by a farmer across all orders
     * OPTIMIZATION: Using previously unused findByFarmerEmail
     */
    public ApiResponse<List<OrderItem>> getAllFarmerItems(String farmerEmail) {
        try {
            // Verify farmer exists
            Farmer farmer = farmerRepo.findByFarmerEmail(farmerEmail);
            if (farmer == null) {
                return ApiResponse.error("Failed to retrieve items", "Farmer not found");
            }

            List<OrderItem> items = orderItemRepo.findByFarmerEmail(farmerEmail);
            return ApiResponse.success("All farmer items retrieved successfully", items);
        } catch (Exception e) {
            logger.error("Failed to retrieve farmer items: {}", e.getMessage(), e);
            return ApiResponse.error("Failed to retrieve items", e.getMessage());
        }
    }

    /**
     * Get filtered orders for a farmer
     * FILTERED: Returns orders with only this farmer's items
     */
    public ApiResponse<List<Order>> getFarmerFilteredOrders(String farmerEmail) {
        try {
            // Verify farmer exists
            Farmer farmer = farmerRepo.findByFarmerEmail(farmerEmail);
            if (farmer == null) {
                return ApiResponse.error("Failed to retrieve orders", "Farmer not found");
            }

            // Get all orders containing at least one product from this farmer
            List<Order> unfiltered = orderRepo.findPlacedOrdersContainingFarmerProducts(farmerEmail);

            // Create filtered copies that only contain this farmer's items
            List<Order> filtered = new ArrayList<>();
            for (Order order : unfiltered) {
                Set<OrderItem> farmerItems = orderItemRepo.findByOrderIdAndFarmerEmail(
                        order.getOrderId(), farmerEmail);

                if (!farmerItems.isEmpty()) {
                    Order filteredOrder = createFilteredOrder(order, farmerItems);
                    filtered.add(filteredOrder);
                }
            }

            return ApiResponse.success("Farmer orders retrieved successfully", filtered);
        } catch (Exception e) {
            logger.error("Failed to retrieve farmer orders: {}", e.getMessage(), e);
            return ApiResponse.error("Failed to retrieve orders", e.getMessage());
        }
    }

    /**
     * Helper method to create a filtered copy of an order with only specific items
     */
    private Order createFilteredOrder(Order originalOrder, Set<OrderItem> itemsToInclude) {
        Order filteredOrder = new Order();

        // Copy order metadata
        filteredOrder.setOrderId(originalOrder.getOrderId());
        filteredOrder.setOrderStatus(originalOrder.getOrderStatus());
        filteredOrder.setCreatedAt(originalOrder.getCreatedAt());
        filteredOrder.setUpdatedAt(originalOrder.getUpdatedAt());
        filteredOrder.setPlacedAt(originalOrder.getPlacedAt());

        // Copy shipping info
        filteredOrder.setShippingAddress(originalOrder.getShippingAddress());
        filteredOrder.setShippingCity(originalOrder.getShippingCity());
        filteredOrder.setShippingState(originalOrder.getShippingState());
        filteredOrder.setShippingZip(originalOrder.getShippingZip());

        // Calculate total only for included items
        double total = itemsToInclude.stream()
                .mapToDouble(item -> item.getQuantity() * (item.getUnitPrice() / item.getQuantity()))
                .sum();

        filteredOrder.setTotalAmount(total);

        // Set items (only those for this farmer)
        filteredOrder.setOrderItems(itemsToInclude);

        // Copy consumer info (but not cart/payment details)
        filteredOrder.setConsumer(originalOrder.getConsumer());

        return filteredOrder;
    }

    /**
     * Get filtered orders for a farmer by item status
     * FILTERED: Returns orders with only this farmer's items
     */
    public ApiResponse<List<Order>> getFarmerOrdersByItemStatus(String farmerEmail, FulfillmentStatus status) {
        try {
            // Verify farmer exists
            Farmer farmer = farmerRepo.findByFarmerEmail(farmerEmail);
            if (farmer == null) {
                return ApiResponse.error("Failed to retrieve orders", "Farmer not found");
            }

            // Get orders with items in the requested status
            List<Order> unfiltered = orderRepo.findOrdersByFarmerAndItemStatus(farmerEmail, status);

            // Create filtered copies that only contain this farmer's items
            List<Order> filtered = new ArrayList<>();
            for (Order order : unfiltered) {
                Set<OrderItem> farmerItems = orderItemRepo.findByOrderIdAndFarmerEmail(
                        order.getOrderId(), farmerEmail);

                if (!farmerItems.isEmpty()) {
                    Order filteredOrder = createFilteredOrder(order, farmerItems);
                    filtered.add(filteredOrder);
                }
            }

            return ApiResponse.success("Orders retrieved successfully", filtered);
        } catch (Exception e) {
            logger.error("Failed to retrieve orders by status: {}", e.getMessage(), e);
            return ApiResponse.error("Failed to retrieve orders", e.getMessage());
        }
    }
    /**
     * Get just the status of an order (lightweight endpoint)
     */
    public ApiResponse<OrderStatus> getOrderStatus(int orderId, String consumerEmail) {
        try {
            Order order = orderRepo.findById(orderId).orElse(null);
            if (order == null) {
                return ApiResponse.error("Order not found", "No order found with ID: " + orderId);
            }

            // Verify ownership
            if (!order.getConsumer().getConsumerEmail().equalsIgnoreCase(consumerEmail)) {
                return ApiResponse.error("Access denied", "You don't have permission to view this order");
            }

            return ApiResponse.success("Order status retrieved", order.getOrderStatus());
        } catch (Exception e) {
            logger.error("Failed to get order status: {}", e.getMessage(), e);
            return ApiResponse.error("Failed to get order status", e.getMessage());
        }
    }

    /**
     * Search orders with filters
     */
    public ApiResponse<List<Order>> searchOrders(String consumerEmail, OrderStatus status,
                                                 String dateFromStr, String dateToStr) {
        try {
            Consumer consumer = consumerRepo.findByConsumerEmail(consumerEmail);
            if (consumer == null) {
                return ApiResponse.error("Consumer not found", "No consumer found with email: " + consumerEmail);
            }

            // Parse dates if provided
            Date dateFrom = null;
            Date dateTo = null;
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

            if (dateFromStr != null && !dateFromStr.isEmpty()) {
                try {
                    dateFrom = format.parse(dateFromStr);
                } catch (ParseException e) {
                    return ApiResponse.error("Invalid date format",
                            "Date format should be yyyy-MM-dd");
                }
            }

            if (dateToStr != null && !dateToStr.isEmpty()) {
                try {
                    dateTo = format.parse(dateToStr);
                    // Set to end of day
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(dateTo);
                    cal.set(Calendar.HOUR_OF_DAY, 23);
                    cal.set(Calendar.MINUTE, 59);
                    cal.set(Calendar.SECOND, 59);
                    dateTo = cal.getTime();
                } catch (ParseException e) {
                    return ApiResponse.error("Invalid date format",
                            "Date format should be yyyy-MM-dd");
                }
            }

            // Search using repository method
            List<Order> orders = orderRepo.searchOrders(consumer.getConsumerId(), status, dateFrom, dateTo);
            return ApiResponse.success("Orders retrieved", orders);
        } catch (Exception e) {
            logger.error("Failed to search orders: {}", e.getMessage(), e);
            return ApiResponse.error("Failed to search orders", e.getMessage());
        }
    }
}