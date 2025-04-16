package com.example.finalyearproject.DataStore;

import com.fasterxml.jackson.annotation.JsonBackReference;
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

    @NotNull(message = "Quantity cannot be null")
    private int Quantity;


    @NotNull(message = "UnitPrice cannot be null")
    private double UnitPrice;

    @ManyToOne()
    @JsonBackReference("order-items")
    private Order order;

    private String FieldChange;

    @ManyToOne
    @JoinColumn(name = "product_id") // explicitly specify join column, if needed.
    @JsonBackReference("order-product")
    private Product product;

}
