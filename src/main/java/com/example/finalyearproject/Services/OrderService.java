package com.example.finalyearproject.Services;

import com.example.finalyearproject.Abstraction.ConsumerRepo;
import com.example.finalyearproject.Abstraction.OrderItemRepo;
import com.example.finalyearproject.Abstraction.OrderRepo;
import com.example.finalyearproject.Abstraction.ProductRepo;
import com.example.finalyearproject.DataStore.Consumer;
import com.example.finalyearproject.DataStore.Order;
import com.example.finalyearproject.DataStore.OrderItem;
import com.example.finalyearproject.DataStore.Product;
import com.example.finalyearproject.Utility.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.Set;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private OrderItemRepo orderItemRepo;

    @Autowired
    private OrderRepo orderRepo;

    @Autowired
    private ConsumerRepo consumerRepo;

    @Autowired
    private ProductRepo productRepo;

    /**
     * Add product to cart
     */
    @Transactional
    public ApiResponse<Set<OrderItem>> addToCart(int consumerId, int productId, int quantity) {
        try {
            // Input validation
            if (quantity <= 0) {
                return ApiResponse.error("Invalid quantity", "Quantity must be a positive number");
            }

            // Get consumer and product
            Consumer consumer = consumerRepo.findConsumerByConsumerId(consumerId);
            Product product = productRepo.findProductByProductId(productId);

            if (product == null) {
                return ApiResponse.error("Product not found", "Product not found with ID: " + productId);
            }

            // Check stock availability
            if (quantity > product.getStock()) {
                return ApiResponse.error("Insufficient stock",
                        "Requested quantity (" + quantity + ") exceeds available stock (" + product.getStock() + ")");
            }

            // Calculate item price with proper decimal handling
            BigDecimal unitPrice = BigDecimal.valueOf(product.getPrice())
                    .multiply(BigDecimal.valueOf(quantity))
                    .setScale(2, RoundingMode.HALF_UP);

            // Find or create cart
            Order cart = findOrCreateCart(consumer);

            // Check if product already exists in cart
            for (OrderItem item : cart.getOrderItems()) {
                if (item.getProduct().getProductId() == productId) {
                    // Check combined quantity against stock
                    int newTotalQuantity = item.getQuantity() + quantity;
                    if (newTotalQuantity > product.getStock()) {
                        return ApiResponse.error("Insufficient stock",
                                "Total quantity (" + newTotalQuantity + ") exceeds available stock (" + product.getStock() + ")");
                    }

                    // Update existing item
                    BigDecimal newItemPrice = BigDecimal.valueOf(product.getPrice())
                            .multiply(BigDecimal.valueOf(newTotalQuantity))
                            .setScale(2, RoundingMode.HALF_UP);

                    item.setQuantity(newTotalQuantity);
                    item.setUnitPrice(newItemPrice.doubleValue());

                    // Update order total
                    BigDecimal additionalAmount = BigDecimal.valueOf(product.getPrice())
                            .multiply(BigDecimal.valueOf(quantity))
                            .setScale(2, RoundingMode.HALF_UP);

                    BigDecimal newTotal = BigDecimal.valueOf(cart.getTotalAmount())
                            .add(additionalAmount)
                            .setScale(2, RoundingMode.HALF_UP);

                    cart.setTotalAmount(newTotal.doubleValue());

                    // Save changes
                    orderItemRepo.save(item);
                    orderRepo.save(cart);

                    return ApiResponse.success("Item quantity updated in cart", cart.getOrderItems());
                }
            }

            // Add new item to cart
            OrderItem newItem = new OrderItem();
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            newItem.setUnitPrice(unitPrice.doubleValue());
            newItem.setOrder(cart);

            // Update product's order items if needed
            if (product.getOrderItems() == null) {
                product.setOrderItems(new HashSet<>());
            }
            product.getOrderItems().add(newItem);

            // Update cart
            cart.getOrderItems().add(newItem);
            BigDecimal newTotal = BigDecimal.valueOf(cart.getTotalAmount())
                    .add(unitPrice)
                    .setScale(2, RoundingMode.HALF_UP);
            cart.setTotalAmount(newTotal.doubleValue());

            // Save everything
            orderItemRepo.save(newItem);
            orderRepo.save(cart);

            return ApiResponse.success("Item added to cart", cart.getOrderItems());
        } catch (Exception e) {
            logger.error("Failed to add to cart: {}", e.getMessage(), e);
            return ApiResponse.error("Failed to add item to cart", e.getMessage());
        }
    }

    /**
     * Find existing cart or create a new one
     */
    private Order findOrCreateCart(Consumer consumer) {
        // Check if consumer has any orders
        if (consumer.getConsumerOrder() != null) {
            // Look for an existing cart (order with status CREATED)
            for (Order order : consumer.getConsumerOrder()) {
                if ("CREATED".equals(order.getOrderStatus())) {
                    return order;
                }
            }
        }

        // Create a new cart if none exists
        Order newCart = new Order();
        newCart.setConsumer(consumer);
        newCart.setOrderStatus("CREATED");
        newCart.setTotalAmount(0.0);
        newCart.setOrderItems(new HashSet<>());

        // Save the new cart
        Order savedCart = orderRepo.save(newCart);

        // Initialize consumer's order set if needed
        if (consumer.getConsumerOrder() == null) {
            consumer.setConsumerOrder(new HashSet<>());
        }

        // Add the new cart to consumer's orders
        consumer.getConsumerOrder().add(savedCart);

        return savedCart;
    }

    /**
     * Remove item from cart
     */
    @Transactional
    public ApiResponse<Set<OrderItem>> removeFromCart(int consumerId, int orderItemId, int quantity) {
        try {
            // Input validation
            if (quantity <= 0) {
                return ApiResponse.error("Invalid quantity", "Quantity must be a positive number");
            }

            // Find cart item
            OrderItem orderItem = orderItemRepo.findOrderItemWithStatusCREATED(consumerId, orderItemId);
            if (orderItem == null) {
                return ApiResponse.error("Item not found", "No active cart item found with ID: " + orderItemId);
            }

            // Find cart
            Order cart = orderRepo.findByStatusAndConsumerId("CREATED", consumerId);
            if (cart == null) {
                return ApiResponse.error("Cart not found", "No active cart found");
            }

            Product product = orderItem.getProduct();

            // Handle removing entire item or reducing quantity
            if (quantity >= orderItem.getQuantity()) {
                // Remove entire item

                // Update cart total
                BigDecimal newTotal = BigDecimal.valueOf(cart.getTotalAmount())
                        .subtract(BigDecimal.valueOf(orderItem.getUnitPrice()))
                        .setScale(2, RoundingMode.HALF_UP);
                cart.setTotalAmount(newTotal.doubleValue());

                // Remove from collections and database
                cart.getOrderItems().remove(orderItem);
                if (product != null && product.getOrderItems() != null) {
                    product.getOrderItems().remove(orderItem);
                }
                orderItemRepo.delete(orderItem);

                // Save cart
                orderRepo.save(cart);

                if (cart.getOrderItems().isEmpty()) {
                    return ApiResponse.success("Cart is now empty", new HashSet<>());
                }

                return ApiResponse.success("Item removed from cart", cart.getOrderItems());
            } else {
                // Reduce quantity
                BigDecimal reducedPrice = BigDecimal.valueOf(product.getPrice())
                        .multiply(BigDecimal.valueOf(quantity))
                        .setScale(2, RoundingMode.HALF_UP);

                // Update item
                int newQuantity = orderItem.getQuantity() - quantity;
                BigDecimal newItemPrice = BigDecimal.valueOf(product.getPrice())
                        .multiply(BigDecimal.valueOf(newQuantity))
                        .setScale(2, RoundingMode.HALF_UP);

                orderItem.setQuantity(newQuantity);
                orderItem.setUnitPrice(newItemPrice.doubleValue());

                // Update cart total
                BigDecimal newTotal = BigDecimal.valueOf(cart.getTotalAmount())
                        .subtract(reducedPrice)
                        .setScale(2, RoundingMode.HALF_UP);
                cart.setTotalAmount(newTotal.doubleValue());

                // Save changes
                orderItemRepo.save(orderItem);
                orderRepo.save(cart);

                return ApiResponse.success("Item quantity reduced in cart", cart.getOrderItems());
            }
        } catch (Exception e) {
            logger.error("Failed to remove from cart: {}", e.getMessage(), e);
            return ApiResponse.error("Failed to remove item from cart", e.getMessage());
        }
    }

    /**
     * Get consumer's cart
     */
    public ApiResponse<Set<OrderItem>> getConsumerCart(int consumerId) {
        try {
            // Find cart
            Order cart = orderRepo.getConsumersCart(consumerId);
            if (cart == null) {
                return ApiResponse.success("Cart is empty", new HashSet<>());
            }

            // Get cart items
            Set<OrderItem> orderItems = cart.getOrderItems();
            if (orderItems == null || orderItems.isEmpty()) {
                return ApiResponse.success("Cart is empty", new HashSet<>());
            }

            return ApiResponse.success("Cart retrieved successfully", orderItems);
        } catch (Exception e) {
            logger.error("Failed to get cart: {}", e.getMessage(), e);
            return ApiResponse.error("Failed to retrieve cart", e.getMessage());
        }
    }

    /**
     * Acknowledge changes in cart items
     */
    @Transactional
    public ApiResponse<Set<OrderItem>> acknowledgeChanges(int consumerId) {
        try {
            // Find cart
            Order cart = orderRepo.getConsumersCart(consumerId);
            if (cart == null) {
                return ApiResponse.success("Cart is empty", new HashSet<>());
            }

            // Get cart items
            Set<OrderItem> orderItems = cart.getOrderItems();
            if (orderItems == null || orderItems.isEmpty()) {
                return ApiResponse.success("Cart is empty", new HashSet<>());
            }

            // Clear change flags
            for (OrderItem item : orderItems) {
                if (item.getFieldChange() != null) {
                    item.setFieldChange(null);
                    orderItemRepo.save(item);
                }
            }

            return ApiResponse.success("Changes acknowledged successfully", orderItems);
        } catch (Exception e) {
            logger.error("Failed to acknowledge changes: {}", e.getMessage(), e);
            return ApiResponse.error("Failed to acknowledge changes", e.getMessage());
        }
    }
}