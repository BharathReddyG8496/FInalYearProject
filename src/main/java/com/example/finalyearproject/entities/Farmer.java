package com.example.finalyearproject.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Farmer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int FarmerId;

    @NotBlank(message = "Name cannot be null")
    private String Name;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    private String Email;

    @NotNull(message = "Phone number cannot be null")
    @Column(unique = true)
    private int Phone;

    @NotBlank(message = "Address cannot be null")
    @Size(max = 255)
    private String Address;

    @OneToMany(mappedBy = "farmer",cascade = CascadeType.ALL)
    private List<Rating> rating;

    @OneToMany(mappedBy = "farmer",cascade = CascadeType.ALL)
    private List<Donation> donation;

    @OneToMany(mappedBy = "farmer",cascade = CascadeType.ALL)
    private List<Product> product;



}
