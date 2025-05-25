package com.example.finalyearproject.Controllers;

import com.example.finalyearproject.DataStore.Consumer;
import com.example.finalyearproject.DataStore.Order;
import com.example.finalyearproject.DataStore.OrderItem;
import com.example.finalyearproject.DataStore.OrderStatus;
import com.example.finalyearproject.Mappers.OrderMapper;
import com.example.finalyearproject.Services.ConsumerService;
import com.example.finalyearproject.Services.OrderService;
import com.example.finalyearproject.Utility.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ConsumerService consumerService;

    @Autowired
    private OrderMapper orderMapper;

    /**
     * Place order
     */
    @PostMapping("/place")
    @PreAuthorize("hasAuthority('CONSUMER')")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> placeOrder(
            @Valid @RequestBody OrderPlacementDTO placementDTO,
            Authentication authentication) {

        Consumer consumer = consumerService.findByEmail(authentication.getName());
        if (consumer == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Consumer not found", "Authentication failed"));
        }

        ApiResponse<Order> response = orderService.placeOrder(consumer.getConsumerId(), placementDTO);

        if (response.getData() != null) {
            OrderResponseDTO orderDTO = orderMapper.toOrderResponseDTO(response.getData());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(response.getMessage(), orderDTO));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(response.getMessage(), response.getErrors()));
        }
    }

    /**
     * Get order history for the authenticated user
     */
    @GetMapping
    @PreAuthorize("hasAuthority('CONSUMER')")
    public ResponseEntity<ApiResponse<List<OrderSummaryDTO>>> getConsumerOrders(Authentication authentication) {
        Consumer consumer = consumerService.findByEmail(authentication.getName());
        if (consumer == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Consumer not found", "Authentication failed"));
        }

        List<Order> orders = orderService.getOrderHistory(consumer.getConsumerId());
        List<OrderSummaryDTO> orderSummaries = orders.stream()
                .map(orderMapper::toOrderSummaryDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("Orders retrieved successfully", orderSummaries));
    }

    /**
     * Get specific order details
     */
    @GetMapping("/{orderId}")
    @PreAuthorize("hasAuthority('CONSUMER')")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> getOrderDetails(
            @PathVariable int orderId,
            Authentication authentication) {

        Consumer consumer = consumerService.findByEmail(authentication.getName());
        if (consumer == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Consumer not found", "Authentication failed"));
        }

        Order order = orderService.getOrderById(orderId);

        // Verify order belongs to this consumer
        if (order == null || order.getConsumer().getConsumerId() != consumer.getConsumerId()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Order not found", "No such order found"));
        }

        OrderResponseDTO orderDTO = orderMapper.toOrderResponseDTO(order);
        return ResponseEntity.ok(ApiResponse.success("Order details retrieved", orderDTO));
    }

    /**
     * Get specific order item details
     */
    @GetMapping("/items/{orderItemId}")
    @PreAuthorize("hasAuthority('CONSUMER')")
    public ResponseEntity<ApiResponse<OrderItemResponseDTO>> getOrderItemDetails(
            @PathVariable int orderItemId,
            Authentication authentication) {

        String consumerEmail = authentication.getName();
        ApiResponse<OrderItem> response = orderService.getOrderItemDetails(orderItemId, consumerEmail);

        if (response.isSuccess()) {
            OrderItemResponseDTO itemDTO = orderMapper.toOrderItemResponseDTO(response.getData());
            return ResponseEntity.ok(ApiResponse.success(response.getMessage(), itemDTO));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(response.getMessage(), response.getErrors()));
        }
    }

    /**
     * Confirm receipt of specific items
     */
    @PutMapping("/{orderId}/items/confirm")
    @PreAuthorize("hasAuthority('CONSUMER')")
    public ResponseEntity<ApiResponse<List<OrderItemResponseDTO>>> confirmItemsReceipt(
            @PathVariable int orderId,
            @RequestBody OrderItemStatusUpdateDTO updateDTO,
            Authentication authentication) {

        String consumerEmail = authentication.getName();
        ApiResponse<List<OrderItem>> response = orderService.confirmItemsReceipt(
                orderId, updateDTO.getOrderItemIds(), consumerEmail);

        if (response.isSuccess()) {
            List<OrderItemResponseDTO> itemDTOs = response.getData().stream()
                    .map(orderMapper::toOrderItemResponseDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success(response.getMessage(), itemDTOs));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(response.getMessage(), response.getErrors()));
        }
    }

    /**
     * Cancel specific items in an order
     */
    @PutMapping("/{orderId}/items/cancel")
    @PreAuthorize("hasAuthority('CONSUMER')")
    public ResponseEntity<ApiResponse<List<OrderItemResponseDTO>>> cancelOrderItems(
            @PathVariable int orderId,
            @RequestBody OrderItemStatusUpdateDTO updateDTO,
            Authentication authentication) {

        String consumerEmail = authentication.getName();
        ApiResponse<List<OrderItem>> response = orderService.cancelOrderItems(
                orderId, updateDTO.getOrderItemIds(), consumerEmail, "CONSUMER", updateDTO.getNotes());

        if (response.isSuccess()) {
            List<OrderItemResponseDTO> itemDTOs = response.getData().stream()
                    .map(orderMapper::toOrderItemResponseDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success(response.getMessage(), itemDTOs));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(response.getMessage(), response.getErrors()));
        }
    }

    /**
     * Cancel entire order
     */
    @PutMapping("/{orderId}/cancel")
    @PreAuthorize("hasAuthority('CONSUMER')")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> cancelOrder(
            @PathVariable int orderId,
            @RequestParam(required = false) String reason,
            Authentication authentication) {

        String consumerEmail = authentication.getName();
        ApiResponse<Order> response = orderService.cancelOrder(orderId, consumerEmail, reason);

        if (response.isSuccess()) {
            OrderResponseDTO orderDTO = orderMapper.toOrderResponseDTO(response.getData());
            return ResponseEntity.ok(ApiResponse.success(response.getMessage(), orderDTO));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(response.getMessage(), response.getErrors()));
        }
    }

    /**
     * Get order status - NEW METHOD
     * Provides a lightweight endpoint to check order status without retrieving all details
     */
    @GetMapping("/{orderId}/status")
    @PreAuthorize("hasAuthority('CONSUMER')")
    public ResponseEntity<ApiResponse<OrderStatusDTO>> getOrderStatus(
            @PathVariable int orderId,
            Authentication authentication) {

        String consumerEmail = authentication.getName();
        ApiResponse<OrderStatus> response = orderService.getOrderStatus(orderId, consumerEmail);

        if (response.isSuccess()) {
            OrderStatusDTO statusDTO = new OrderStatusDTO();
            statusDTO.setOrderId(orderId);
            statusDTO.setStatus(response.getData());

            return ResponseEntity.ok(ApiResponse.success(response.getMessage(), statusDTO));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(response.getMessage(), response.getErrors()));
        }
    }

    /**
     * Search orders - NEW METHOD
     * Allows searching through order history with filters
     */
    @GetMapping("/search")
    @PreAuthorize("hasAuthority('CONSUMER')")
    public ResponseEntity<ApiResponse<List<OrderSummaryDTO>>> searchOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            Authentication authentication) {

        String consumerEmail = authentication.getName();
        ApiResponse<List<Order>> response = orderService.searchOrders(consumerEmail, status, dateFrom, dateTo);

        if (response.isSuccess()) {
            List<OrderSummaryDTO> orderSummaries = response.getData().stream()
                    .map(orderMapper::toOrderSummaryDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success(response.getMessage(), orderSummaries));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(response.getMessage(), response.getErrors()));
        }
    }

    @GetMapping("/statuses")
    public ResponseEntity<ApiResponse<Map<String, String>>> getOrderStatuses() {
        // If you only want to return certain statuses that are relevant for filtering
        // you can manually specify them instead of returning all enum values
        List<String> statuses = Arrays.asList(
                OrderStatus.PLACED.name(),
                OrderStatus.DELIVERED.name(),
                OrderStatus.COMPLETED.name(),
                OrderStatus.CANCELLED.name()
        );

        // Optionally add descriptions for each status
        Map<String, String> statusesWithDescriptions = new LinkedHashMap<>();
        statusesWithDescriptions.put(OrderStatus.PLACED.name(), "Order is placed but not all items delivered");
        statusesWithDescriptions.put(OrderStatus.DELIVERED.name(), "All items delivered but not confirmed");
        statusesWithDescriptions.put(OrderStatus.COMPLETED.name(), "All items confirmed by customer");
        statusesWithDescriptions.put(OrderStatus.CANCELLED.name(), "Order was cancelled");

        return ResponseEntity.ok(ApiResponse.success("Order statuses retrieved", statusesWithDescriptions));

        // Alternative if you want to return with descriptions:
        // return ResponseEntity.ok(ApiResponse.success("Order statuses retrieved", statusesWithDescriptions));
    }
}