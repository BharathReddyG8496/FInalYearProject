package com.example.finalyearproject.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int OrderItemId;

    @NotNull
    private int OrderId;

    @NotNull(message = "Quantity cannot be null")
    @Positive
    private int Quantity;

    @NotNull(message = "ProductId cannot be null")
    private int ProductId;

    @NotNull(message = "UnitPrice cannot be null")
    @Positive
    private double UnitPrice;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "OrderId",insertable = false,updatable = false)
    private Order order;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "ProductId",insertable = false,updatable = false)
    private Product product;

}
