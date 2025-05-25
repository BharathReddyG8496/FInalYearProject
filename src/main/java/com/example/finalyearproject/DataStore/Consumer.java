package com.example.finalyearproject.DataStore;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class Consumer implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int consumerId;

    @NotBlank(message = "First Name cannot be blank")
    private String consumerFirstName;

    @NotBlank(message = "Last Name cannot be blank")
    private String consumerLastName;

    @NotBlank(message = "Password cannot be blank")
    @JsonIgnore
    private String consumerPassword;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    @Column(unique = true)
    private String consumerEmail;

    @NotNull(message = "Phone number cannot be null")
    @Pattern(regexp = "^(\\+91|0)?\\d{10}$")
    @Column(unique = true)
    private String consumerPhone;

    private String profilePhotoPath;


    @NotBlank(message = "Address cannot be blank")
    @Size(max = 255, message = "Address cannot exceed 255 characters")
    private String consumerAddress;

    @OneToMany(mappedBy = "consumer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("consumer-ratings")
    @JsonIgnore
    private Set<Rating> consumerRatings;


    @OneToMany(mappedBy = "consumer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("consumer-donations")
    @JsonIgnore
    private Set<Donation> consumerDonations;

    @OneToMany(mappedBy = "consumer", cascade = CascadeType.ALL)
    @JsonManagedReference("consumer-order")
    @JsonIgnore
    private Set<Order> consumerOrder;

    @OneToMany(mappedBy = "consumer",cascade = CascadeType.ALL)
    @JsonManagedReference("consumer-addresses")
    @JsonIgnore
    private Set<DeliveryAddresses> setOfDeliveryAddress;

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Return CONSUMER authority
        return Collections.singletonList(new SimpleGrantedAuthority("CONSUMER"));
    }

    @Override
    @JsonIgnore
    public String getPassword() {
        return this.consumerPassword;
    }

    @JsonIgnore
    public String getFarmerName() {
        // If firstName or lastName is null, avoid NullPointerException
        if(consumerFirstName == null || consumerLastName == null) {
            return null;
        }
        return consumerFirstName.concat(consumerLastName);
    }

    @Override
    @JsonIgnore
    public String getUsername() {
        return getFarmerName();
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return true;
    }
}
