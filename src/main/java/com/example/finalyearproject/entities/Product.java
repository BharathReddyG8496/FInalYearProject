package com.example.finalyearproject.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int ProductId;

    @NotNull(message = "cannot be null")
    private int FarmerId;

    @NotNull(message = "Product name cannot be null")
    @Column(length = 100)
    private String Name;

    @NotNull(message = " Description cannot be null")
    @Lob
    private String Description;

    @NotNull(message = "Price cannot be null")
    @Positive
    private double Price;

    @Positive
    @Min(value = 0)
    private int Stock;

    @OneToMany(mappedBy = "product",cascade = CascadeType.ALL)
    private OrderItem orderItem;


}
