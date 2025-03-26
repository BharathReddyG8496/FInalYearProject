package com.example.finalyearproject.DataStore;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int OrderId;

//    private int ConsumerId;

//    @NotNull(message ="Date cannot be null")
    private LocalDateTime OrderDate;

//    @NotNull(message = "Amount can not be null")
//    @Positive
    private double TotalAmount;

    @ManyToOne()
    @JsonBackReference("consumer-order")
    private Consumer consumer;

    @OneToMany(mappedBy = "order",cascade = CascadeType.ALL)
    @JsonManagedReference("order-items")
    private Set<OrderItem> orderItems;

    @OneToOne
    @JsonBackReference("delivery-order")
    private DeliveryAddresses deliveryAddress;
}
