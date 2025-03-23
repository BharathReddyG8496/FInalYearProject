package com.example.finalyearproject.DataStore;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Set;

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
    @Pattern(regexp = "\\+\\{12}")
    private int Phone;

    @NotBlank(message = "Address cannot be null")
    @Size(max = 255)
    private String Address;

    @OneToMany(cascade = CascadeType.ALL)
    @JsonManagedReference
    private Set<Rating> rating;

    @OneToMany(cascade = CascadeType.ALL)
    @JsonManagedReference
    private Set<Donation> donation;

    @OneToMany(cascade = CascadeType.ALL)
    @JsonManagedReference
    private Set<Product> product;



}
