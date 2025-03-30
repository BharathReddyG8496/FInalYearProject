package com.example.finalyearproject.Utility;

import com.example.finalyearproject.DataStore.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderUtility {
    int statusCode;
    String message;
    Set<OrderItem> orderItems;
}
