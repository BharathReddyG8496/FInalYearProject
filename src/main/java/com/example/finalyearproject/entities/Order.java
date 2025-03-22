package com.example.finalyearproject.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer OrderId;

    private int ConsumerId;

    @NotNull(message ="Date cannot be null")
    private LocalDateTime OrderDate;

    @NotNull(message = "Amount can not be null")
    @Positive
    private double TotalAmount;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "ConsumerId",insertable = false,updatable = false)
    private Consumer consumer;

    @OneToMany(mappedBy = "order",cascade = CascadeType.ALL)
    private List<OrderItem> orderItems;

    @OneToMany(mappedBy="order",cascade = CascadeType.ALL)
    private Delivery delivery;
}
