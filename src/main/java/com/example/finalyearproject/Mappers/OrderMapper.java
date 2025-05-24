package com.example.finalyearproject.Mappers;


import com.example.finalyearproject.DataStore.FulfillmentStatus;
import com.example.finalyearproject.DataStore.Order;
import com.example.finalyearproject.DataStore.OrderItem;
import com.example.finalyearproject.Utility.OrderItemResponseDTO;
import com.example.finalyearproject.Utility.OrderResponseDTO;
import com.example.finalyearproject.Utility.OrderSummaryDTO;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    public OrderResponseDTO toOrderResponseDTO(Order order) {
        if (order == null) {
            return null;
        }

        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setOrderId(order.getOrderId());
        dto.setStatus(order.getOrderStatus());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setPlacedAt(order.getPlacedAt());
        dto.setShippingAddress(order.getShippingAddress());
        dto.setShippingCity(order.getShippingCity());
        dto.setShippingState(order.getShippingState());
        dto.setShippingZip(order.getShippingZip());

        List<OrderItemResponseDTO> items = order.getOrderItems().stream()
                .map(this::toOrderItemResponseDTO)
                .collect(Collectors.toList());

        dto.setItems(items);

        return dto;
    }

    public OrderSummaryDTO toOrderSummaryDTO(Order order) {
        if (order == null) {
            return null;
        }

        OrderSummaryDTO dto = new OrderSummaryDTO();
        dto.setOrderId(order.getOrderId());
        dto.setStatus(order.getOrderStatus());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setPlacedAt(order.getPlacedAt());
        dto.setShippingAddress(order.getShippingAddress());
        dto.setShippingCity(order.getShippingCity());
        dto.setShippingState(order.getShippingState());
        dto.setShippingZip(order.getShippingZip());

        // Count items by status
        Map<String, Integer> itemCounts = new HashMap<>();
        for (OrderItem item : order.getOrderItems()) {
            String status = item.getFulfillmentStatus().toString();
            itemCounts.put(status, itemCounts.getOrDefault(status, 0) + 1);
        }

        dto.setItemCounts(itemCounts);

        return dto;
    }

    public OrderItemResponseDTO toOrderItemResponseDTO(OrderItem item) {
        if (item == null) {
            return null;
        }

        return OrderItemResponseDTO.builder()
                .orderItemId(item.getOrderItemId())
                .productId(item.getProduct() != null ? item.getProduct().getProductId() : null)
                .productName(item.getProduct() != null ? item.getProduct().getName() : null)
                .productImage(item.getProduct() != null && item.getProduct().getImages() != null &&
                        !item.getProduct().getImages().isEmpty() ?
                        item.getProduct().getImages().iterator().next().getFilePath() : null)
                .unitPrice(item.getQuantity() > 0 ? item.getUnitPrice() / item.getQuantity() : 0)
                .quantity(item.getQuantity())
                .totalPrice(item.getUnitPrice())
                .status(item.getFulfillmentStatus())
                .farmerName(item.getProduct() != null && item.getProduct().getFarmer() != null ?
                        item.getProduct().getFarmer().getFarmerName() : null)
                .deliveredAt(item.getDeliveredAt())
                .confirmedAt(item.getConfirmedAt())
                .deliveryNotes(item.getDeliveryNotes())
                .canBeRated(item.getFulfillmentStatus() == FulfillmentStatus.CONFIRMED && !item.isRated())
                .build();
    }
}