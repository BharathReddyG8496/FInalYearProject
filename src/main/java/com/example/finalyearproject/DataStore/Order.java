package com.example.finalyearproject.DataStore;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
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
    @JsonBackReference
//    @JoinColumn(name = "ConsumerId",updatable = false)
    private Consumer consumer;

    @OneToMany(mappedBy = "order",cascade = CascadeType.ALL)
    @JsonManagedReference
    private Set<OrderItem> orderItems;

    @OneToMany(mappedBy = "order",cascade = CascadeType.ALL)
    @JsonManagedReference
    private Set<Delivery> delivery;
}
