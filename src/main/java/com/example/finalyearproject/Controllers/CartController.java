package com.example.finalyearproject.Controllers;

import com.example.finalyearproject.DataStore.Consumer;
import com.example.finalyearproject.DataStore.Order;
import com.example.finalyearproject.DataStore.OrderItem;
import com.example.finalyearproject.Mappers.CartMapper;
import com.example.finalyearproject.Services.CartService;
import com.example.finalyearproject.Services.ConsumerService;
import com.example.finalyearproject.Utility.ApiResponse;
import com.example.finalyearproject.Utility.CartResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private ConsumerService consumerService;

    @Autowired
    private CartMapper cartMapper;

    /**
     * Add item to cart
     */
    @PostMapping("/add")
    @PreAuthorize("hasAuthority('CONSUMER')")
    public ResponseEntity<ApiResponse<CartResponseDTO>> addToCart(
            @RequestParam int productId,
            @RequestParam int quantity,
            Authentication authentication) {

        Consumer consumer = consumerService.findByEmail(authentication.getName());
        if (consumer == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Consumer not found", "Authentication failed"));
        }

        ApiResponse<Set<OrderItem>> response =
                cartService.addToCart(consumer.getConsumerId(), productId, quantity);

        return handleCartOperationResponse(response, consumer.getConsumerId());
    }

    /**
     * Remove item from cart
     */
    @DeleteMapping("/{orderItemId}")
    @PreAuthorize("hasAuthority('CONSUMER')")
    public ResponseEntity<ApiResponse<CartResponseDTO>> removeFromCart(
            @PathVariable int orderItemId,
            @RequestParam int quantity,
            Authentication authentication) {

        Consumer consumer = consumerService.findByEmail(authentication.getName());
        if (consumer == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Consumer not found", "Authentication failed"));
        }

        ApiResponse<Set<OrderItem>> response =
                cartService.removeFromCart(consumer.getConsumerId(), orderItemId, quantity);

        return handleCartOperationResponse(response, consumer.getConsumerId());
    }

    /**
     * Update item quantity
     */
    @PutMapping("/{orderItemId}")
    @PreAuthorize("hasAuthority('CONSUMER')")
    public ResponseEntity<ApiResponse<CartResponseDTO>> updateQuantity(
            @PathVariable int orderItemId,
            @RequestParam int quantity,
            Authentication authentication) {

        Consumer consumer = consumerService.findByEmail(authentication.getName());
        if (consumer == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Consumer not found", "Authentication failed"));
        }

        if (quantity <= 0) {
            // If quantity is 0 or negative, remove item completely
            ApiResponse<Set<OrderItem>> response =
                    cartService.removeFromCart(consumer.getConsumerId(), orderItemId, Integer.MAX_VALUE);

            return handleCartOperationResponse(response, consumer.getConsumerId(), "Item removed from cart");
        } else {
            // First get the current item to determine if we need to add or remove
            ApiResponse<Order> currentCart = cartService.getCart(consumer.getConsumerId());
            OrderItem currentItem = null;

            if (currentCart.getData() != null) {
                for (OrderItem item : currentCart.getData().getOrderItems()) {
                    if (item.getOrderItemId() == orderItemId) {
                        currentItem = item;
                        break;
                    }
                }
            }

            ApiResponse<Set<OrderItem>> response;
            String message;

            if (currentItem == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Item not found", "No such item in your cart"));
            } else if (quantity > currentItem.getQuantity()) {
                // Add the difference
                int toAdd = quantity - currentItem.getQuantity();
                response = cartService.addToCart(consumer.getConsumerId(),
                        currentItem.getProduct().getProductId(), toAdd);
                message = "Item quantity increased";
            } else if (quantity < currentItem.getQuantity()) {
                // Remove the difference
                int toRemove = currentItem.getQuantity() - quantity;
                response = cartService.removeFromCart(consumer.getConsumerId(), orderItemId, toRemove);
                message = "Item quantity decreased";
            } else {
                // No change needed
                ApiResponse<Order> cartResponse = cartService.getCart(consumer.getConsumerId());
                CartResponseDTO cartDTO = cartMapper.toCartResponseDTO(cartResponse.getData());
                return ResponseEntity.ok(ApiResponse.success("No change in quantity", cartDTO));
            }

            return handleCartOperationResponse(response, consumer.getConsumerId(), message);
        }
    }

    /**
     * Get cart
     */
    @GetMapping
    @PreAuthorize("hasAuthority('CONSUMER')")
    public ResponseEntity<ApiResponse<CartResponseDTO>> getCart(Authentication authentication) {
        Consumer consumer = consumerService.findByEmail(authentication.getName());
        if (consumer == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Consumer not found", "Authentication failed"));
        }

        ApiResponse<Order> response = cartService.getCart(consumer.getConsumerId());

        // Convert to DTO
        CartResponseDTO cartDTO = cartMapper.toCartResponseDTO(response.getData());

        return ResponseEntity.ok(ApiResponse.success(response.getMessage(), cartDTO));
    }

    /**
     * Delete item from cart completely
     */
    @DeleteMapping("/item/{orderItemId}")
    @PreAuthorize("hasAuthority('CONSUMER')")
    public ResponseEntity<ApiResponse<CartResponseDTO>> deleteCartItem(
            @PathVariable int orderItemId,
            Authentication authentication) {

        Consumer consumer = consumerService.findByEmail(authentication.getName());
        if (consumer == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Consumer not found", "Authentication failed"));
        }

        ApiResponse<Set<OrderItem>> response = cartService.deleteCartItem(consumer.getConsumerId(), orderItemId);

        return handleCartOperationResponse(response, consumer.getConsumerId());
    }

    /**
     * Acknowledge cart changes
     */
    @PutMapping("/acknowledge-changes")
    @PreAuthorize("hasAuthority('CONSUMER')")
    public ResponseEntity<ApiResponse<CartResponseDTO>> acknowledgeChanges(Authentication authentication) {
        Consumer consumer = consumerService.findByEmail(authentication.getName());
        if (consumer == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Consumer not found", "Authentication failed"));
        }

        ApiResponse<Set<OrderItem>> response =
                cartService.acknowledgeChanges(consumer.getConsumerId());

        return handleCartOperationResponse(response, consumer.getConsumerId());
    }

    /**
     * Helper method to handle common cart operation response processing
     */
    private ResponseEntity<ApiResponse<CartResponseDTO>> handleCartOperationResponse(
            ApiResponse<Set<OrderItem>> response,
            int consumerId) {
        return handleCartOperationResponse(response, consumerId, response.getMessage());
    }

    /**
     * Helper method to handle common cart operation response processing with custom message
     */
    private ResponseEntity<ApiResponse<CartResponseDTO>> handleCartOperationResponse(
            ApiResponse<Set<OrderItem>> response,
            int consumerId,
            String successMessage) {

        if (!response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(response.getMessage(), response.getErrors()));
        }

        // Get updated cart and convert to DTO
        ApiResponse<Order> cartResponse = cartService.getCart(consumerId);
        CartResponseDTO cartDTO = cartMapper.toCartResponseDTO(cartResponse.getData());

        return ResponseEntity.ok(ApiResponse.success(successMessage, cartDTO));
    }
}