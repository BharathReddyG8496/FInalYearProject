package com.example.finalyearproject.DataStore;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Farmer implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int farmerId;

    @NotBlank(message = "Name cannot be null")
    private String farmerName;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    @Column(unique = true)
    private String farmerEmail;

    @NotNull
    private String firstName;

    @NotNull
    private String lastName;

    @NotNull
    private String farmerPassword;

    @NotNull(message = "Phone number cannot be null")
    @Column(unique = true)
    @Pattern(regexp = "^(\\+91|0)?\\d{10}$")
    private String farmerPhone;

    @NotBlank(message = "Address cannot be null")
    @Size(max = 255)
    private String farmerAddress;

    @OneToMany(cascade = CascadeType.ALL)
    @JsonManagedReference("farmer-donations")
    private Set<Donation> farmerDonations;

    @OneToMany(mappedBy = "farmer", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("farmer-product")
    private Set<Product> farmerProducts = new HashSet<>();


    // Aggregate rating fields
    private Double totalRating = 0.0;
    private Integer ratingCount = 0;
    private Double averageRating = 0.0;

    // UserDetails methods implementation
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null; // or your authorities implementation
    }

    @Override
    public String getPassword() {
        return this.farmerPassword;
    }

    @Override
    public String getUsername() {
        return this.farmerName;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
