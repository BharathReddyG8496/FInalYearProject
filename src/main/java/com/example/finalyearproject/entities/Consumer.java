package com.example.finalyearproject.entities;

import jakarta.persistence.*;
import lombok.Data;


@Entity
@Data
public class Consumer {
    @Id
    @GeneratedValue
    private int ConsumerId;
    private String Name;
    @Column(unique = true)
    private String Email;
    @Column(unique = true)
    private int Phone;
    private String Address;

}
