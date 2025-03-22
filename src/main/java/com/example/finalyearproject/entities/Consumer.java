package com.example.finalyearproject.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@AllArgsConstructor
@NoArgsConstructor
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
    @OneToMany(mappedBy = "consumer",cascade = CascadeType.ALL)
    private Rating rating;


}
