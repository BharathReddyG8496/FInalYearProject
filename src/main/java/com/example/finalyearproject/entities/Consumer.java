package com.example.finalyearproject.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;


@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
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
    private List<Rating> rating;


}
