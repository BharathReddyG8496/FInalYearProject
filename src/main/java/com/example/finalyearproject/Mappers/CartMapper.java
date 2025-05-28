package com.example.finalyearproject.Mappers;

import com.example.finalyearproject.DataStore.Order;
import com.example.finalyearproject.DataStore.OrderItem;
import com.example.finalyearproject.Utility.CartItemResponseDTO;
import com.example.finalyearproject.Utility.CartResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CartMapper {
    public CartResponseDTO toCartResponseDTO(Order cart) {
        if (cart == null) {
            return null;
        }

        CartResponseDTO dto = new CartResponseDTO();
        dto.setCartId(cart.getOrderId());
        dto.setTotalAmount(cart.getTotalAmount());

        List<CartItemResponseDTO> items = cart.getOrderItems().stream()
                .map(this::toCartItemResponseDTO)
                .collect(Collectors.toList());

        dto.setItems(items);
        dto.setItemCount(items.size());

        return dto;
    }

    public CartItemResponseDTO toCartItemResponseDTO(OrderItem item) {
        CartItemResponseDTO dto = new CartItemResponseDTO();
        dto.setOrderItemId(item.getOrderItemId());
        dto.setProductId(item.getProductId());
        dto.setProductName(item.getProductName());
        // Set product image if available
        if (item.getProduct() != null && item.getProduct().getImages() != null &&
                !item.getProduct().getImages().isEmpty()) {
            dto.setProductImage(item.getProduct().getImages().iterator().next().getFilePath());
        }
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice() / item.getQuantity()); // Get per-unit price
        dto.setTotalPrice(item.getUnitPrice());
        dto.setFarmerName(item.getFarmerName());
        dto.setFieldChange(item.getFieldChange());

        // ADD UNIT
        if (item.getProduct() != null) {
            dto.setUnit(item.getProduct().getUnit());
        }

        return dto;
    }
}