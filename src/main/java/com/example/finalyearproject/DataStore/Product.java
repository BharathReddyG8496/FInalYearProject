package com.example.finalyearproject.DataStore;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int productId;

//    @NotNull(message = "cannot be null")
//    private int FarmerId;

    @NotBlank(message = "Product name cannot be null")
    @Column(length = 100)
    private String name;

    @NotBlank(message = " Description cannot be null")
    private String description;

    @NotNull(message = "Price cannot be null")
    private double price;

    @NotNull(message = "stock cannot be null")
    @Min(value = 0)
    private int stock;

    @OneToMany(cascade = CascadeType.ALL)
    @JsonManagedReference("order-product")
    private Set<OrderItem> orderItem;

    @ManyToOne()
    @JsonBackReference("farmer-product")
//    @JoinColumn(name="FarmerId",insertable = false,updatable = false)
    private Farmer farmer;


}
