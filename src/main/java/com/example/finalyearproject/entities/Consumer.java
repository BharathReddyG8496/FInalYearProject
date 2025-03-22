package com.example.finalyearproject.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;


@Entity
@Getter
@Setter
@NoArgsConstructor
public class Consumer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int ConsumerId;

    @NotBlank(message = "First Name cannot be blank")
    private String FirstName;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    @Column(unique = true)
    private String Email;

    @NotNull(message = "Phone number cannot be null")
    @Min(value = 1000000000, message = "Phone number must be at least 10 digits")
    @Max(value = 9999999999L, message = "Phone number cannot exceed 10 digits")
    @Column(unique = true)
    private int Phone;

    @NotBlank(message = "Address cannot be blank")
    @Size(max = 255, message = "Address cannot exceed 255 characters")
    private String Address;

    @OneToMany(mappedBy = "consumer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Rating> ratings;


    @OneToMany(mappedBy = "consumer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Donation> donations;
}
