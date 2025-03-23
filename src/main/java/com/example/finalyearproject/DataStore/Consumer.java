package com.example.finalyearproject.DataStore;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Set;


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
    @Pattern(regexp = "\\+\\{12}")
//    @Column(unique = true)
    private int Phone;

    @NotBlank(message = "Address cannot be blank")
    @Size(max = 255, message = "Address cannot exceed 255 characters")
    private String Address;

    @OneToMany( cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private Set<Rating> ratings;


    @OneToMany( cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private Set<Donation> donations;

    @OneToMany(cascade = CascadeType.ALL)
    @JsonManagedReference
    private Set<Order> order;
}
