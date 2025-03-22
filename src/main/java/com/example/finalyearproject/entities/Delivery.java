package com.example.finalyearproject.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Delivery {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int DeliveryId;

    private int OrderId;
    @NotNull(message = "DeliveryMethod cannot be null")
    private String DeliveryMethod;

    @NotNull(message = "Status cannot be null")
    private String Status;

    @NotBlank(message = "DeliveryAddress cannot be null")
    @Size(max = 255,message = "Limited to 255 char")
    private String DeliveryAddress;

    @NotNull(message = "Date cannot be null")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime DeliveryDate;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "OrderId",insertable = false,updatable = false)
    private Order order;
}
