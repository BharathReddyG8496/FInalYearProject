package com.example.finalyearproject.Mappers;

import com.example.finalyearproject.DataStore.FulfillmentStatus;
import com.example.finalyearproject.DataStore.Order;
import com.example.finalyearproject.DataStore.OrderItem;
import com.example.finalyearproject.Utility.*;
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

        // Add consumer information
        if (order.getConsumer() != null) {
            ConsumerInfoDTO consumerInfo = new ConsumerInfoDTO();
            consumerInfo.setConsumerId(order.getConsumer().getConsumerId());
            consumerInfo.setConsumerName(order.getConsumer().getConsumerFirstName() + " " +
                    order.getConsumer().getConsumerLastName());
            consumerInfo.setConsumerEmail(order.getConsumer().getConsumerEmail());
            consumerInfo.setConsumerPhone(order.getConsumer().getConsumerPhone());
            dto.setConsumer(consumerInfo);
        }

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

        // Add consumer information
        if (order.getConsumer() != null) {
            ConsumerInfoDTO consumerInfo = new ConsumerInfoDTO();
            consumerInfo.setConsumerId(order.getConsumer().getConsumerId());
            consumerInfo.setConsumerName(order.getConsumer().getConsumerFirstName() + " " +
                    order.getConsumer().getConsumerLastName());
            consumerInfo.setConsumerEmail(order.getConsumer().getConsumerEmail());
            consumerInfo.setConsumerPhone(order.getConsumer().getConsumerPhone());
            dto.setConsumer(consumerInfo);
        }

        // Count items by status (keep this for backward compatibility)
        Map<String, Integer> itemCounts = new HashMap<>();
        for (OrderItem item : order.getOrderItems()) {
            String status = item.getFulfillmentStatus().toString();
            itemCounts.put(status, itemCounts.getOrDefault(status, 0) + 1);
        }
        dto.setItemCounts(itemCounts);

        // Add the list of order item summaries
        List<OrderItemSummaryDTO> itemSummaries = order.getOrderItems().stream()
                .map(item -> {
                    OrderItemSummaryDTO itemDto = new OrderItemSummaryDTO();
                    itemDto.setOrderItemId(item.getOrderItemId());
                    itemDto.setProductId(item.getProduct() != null ? item.getProduct().getProductId() : 0);
                    itemDto.setProductName(item.getProductName());
                    // Get first image if available
                    if (item.getProduct() != null && item.getProduct().getImages() != null &&
                            !item.getProduct().getImages().isEmpty()) {
                        itemDto.setProductImage(item.getProduct().getImages().iterator().next().getFilePath());
                    }
                    itemDto.setStatus(item.getFulfillmentStatus());
                    itemDto.setTotalPrice(item.getUnitPrice());
                    itemDto.setQuantity(item.getQuantity());
                    itemDto.setUnit(item.getProduct() != null ? item.getProduct().getUnit() : null);
                    return itemDto;
                })
                .collect(Collectors.toList());

        dto.setItems(itemSummaries);

        return dto;
    }


    public OrderItemResponseDTO toOrderItemResponseDTO(OrderItem item) {
        if (item == null) {
            return null;
        }

        boolean canDonate = item.getFulfillmentStatus() == FulfillmentStatus.DELIVERED ||
                item.getFulfillmentStatus() == FulfillmentStatus.CONFIRMED;

        return OrderItemResponseDTO.builder()
                .orderItemId(item.getOrderItemId())
                .productId(item.getProduct() != null ? item.getProduct().getProductId() : null)
                .productName(item.getProduct() != null ? item.getProduct().getName() : null)
                .productImage(item.getProduct() != null && item.getProduct().getImages() != null &&
                        !item.getProduct().getImages().isEmpty() ?
                        item.getProduct().getImages().iterator().next().getFilePath() : null)
                .unitPrice(item.getQuantity() > 0 ? item.getUnitPrice() / item.getQuantity() : 0)
                .quantity(item.getQuantity())
                .unit(item.getProduct() != null ? item.getProduct().getUnit() : null)
                .totalPrice(item.getUnitPrice())
                .status(item.getFulfillmentStatus())
                .farmerName(item.getProduct() != null && item.getProduct().getFarmer() != null ?
                        item.getProduct().getFarmer().getFirstName() + " " +
                                item.getProduct().getFarmer().getLastName() : null)
                .farmerId(item.getProduct() != null && item.getProduct().getFarmer() != null ?
                        item.getProduct().getFarmer().getFarmerId() : 0)  // ADD THIS
                .deliveredAt(item.getDeliveredAt())
                .confirmedAt(item.getConfirmedAt())
                .deliveryNotes(item.getDeliveryNotes())
                .canBeRated(item.getFulfillmentStatus() == FulfillmentStatus.CONFIRMED && !item.isRated())
                .canDonate(canDonate)  // ADD THIS
                .build();
    }
}