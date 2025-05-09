package com.example.finalyearproject.Controllers;

import com.example.finalyearproject.DataStore.Consumer;
import com.example.finalyearproject.DataStore.OrderItem;
import com.example.finalyearproject.Services.ConsumerService;
import com.example.finalyearproject.Services.OrderService;
import com.example.finalyearproject.Utility.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/cart")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ConsumerService consumerService;

    /**
     * Add item to cart
     */
    @PostMapping("/items")
    public ResponseEntity<ApiResponse<Set<OrderItem>>> addToCart(
            @RequestParam int productId,
            @RequestParam int quantity,
            Authentication authentication) {

        String email = authentication.getName();
        Consumer consumer = consumerService.findByEmail(email);

        if (consumer == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Failed to add item to cart", "Consumer not found"));
        }

        ApiResponse<Set<OrderItem>> response =
                orderService.addToCart(consumer.getConsumerId(), productId, quantity);

        if (response.getData() != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Remove item from cart
     */
    @DeleteMapping("/items/{orderItemId}")
    public ResponseEntity<ApiResponse<Set<OrderItem>>> removeFromCart(
            @PathVariable int orderItemId,
            @RequestParam int quantity,
            Authentication authentication) {

        String email = authentication.getName();
        Consumer consumer = consumerService.findByEmail(email);

        if (consumer == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Failed to remove item from cart", "Consumer not found"));
        }

        ApiResponse<Set<OrderItem>> response =
                orderService.removeFromCart(consumer.getConsumerId(), orderItemId, quantity);

        if (response.getData() != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Get consumer's cart
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Set<OrderItem>>> getCart(Authentication authentication) {
        String email = authentication.getName();
        Consumer consumer = consumerService.findByEmail(email);

        if (consumer == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Failed to get cart", "Consumer not found"));
        }

        ApiResponse<Set<OrderItem>> response = orderService.getConsumerCart(consumer.getConsumerId());

        if (response.getData() != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Acknowledge cart item changes
     */
    @PutMapping("/acknowledge-changes")
    public ResponseEntity<ApiResponse<Set<OrderItem>>> acknowledgeChanges(Authentication authentication) {
        String email = authentication.getName();
        Consumer consumer = consumerService.findByEmail(email);

        if (consumer == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Failed to acknowledge changes", "Consumer not found"));
        }

        ApiResponse<Set<OrderItem>> response =
                orderService.acknowledgeChanges(consumer.getConsumerId());

        if (response.getData() != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}