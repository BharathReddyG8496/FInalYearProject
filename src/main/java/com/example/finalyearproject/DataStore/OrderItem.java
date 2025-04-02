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

//    @NotNull
//    private int OrderId;

    @NotNull(message = "Quantity cannot be null")
    private int Quantity;

//    @NotNull(message = "ProductId cannot be null")
//    private int ProductId;

    @NotNull(message = "UnitPrice cannot be null")
    private double UnitPrice;

    @ManyToOne()
    @JsonBackReference("order-items")
//    @JoinColumn(name = "OrderId",insertable = false,updatable = false)
    private Order order;

    private String FieldChange;

    @ManyToOne()
    @JsonBackReference("order-product")
//    @JoinColumn(name = "ProductId",insertable = false,updatable = false)
    private Product product;

}
