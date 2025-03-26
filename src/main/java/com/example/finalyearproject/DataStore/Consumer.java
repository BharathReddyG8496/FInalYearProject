package com.example.finalyearproject.DataStore;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Set;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Consumer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int consumerId;

    @NotBlank(message = "First Name cannot be blank")
    private String consumerFirstName;
    private String consumerLastName;

    @NotNull(message = "UserName cannot be blank")
    private String consumerName;
    private String consumerPassword;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    @Column(unique = true)
    private String consumerEmail;

    @NotNull(message = "Phone number cannot be null")
    @Pattern(regexp = "^(\\+91|0)?\\d{10}$")
    @Column(unique = true)
    private String consumerPhone;

    @NotBlank(message = "Address cannot be blank")
    @Size(max = 255, message = "Address cannot exceed 255 characters")
    private String consumerAddress;

    @OneToMany(mappedBy = "consumer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("consumer-ratings")
    private Set<Rating> consumerRatings;


    @OneToMany(mappedBy = "consumer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("consumer-donations")
    private Set<Donation> consumerDonations;

    @OneToMany(mappedBy = "consumer", cascade = CascadeType.ALL)
    @JsonManagedReference("consumer-order")
    private Set<Order> consumerOrder;

    @OneToMany(mappedBy = "consumer",cascade = CascadeType.ALL)
    @JsonManagedReference("consumer-addresses")
    private Set<DeliveryAddresses> setOfDeliveryAddress;

//    @Override
//    public Collection<? extends GrantedAuthority> getAuthorities() {
//        return null;
//    }
//
//    @Override
//    public String getPassword() {
//        return this.consumerPassword;
//    }
//
//    @Override
//    public String getUsername() {
//        return this.consumerName;
//    }
//
//    @Override
//    public boolean isAccountNonExpired() {
//        return true;
//    }
//
//    @Override
//    public boolean isAccountNonLocked() {
//        return true;
//    }
//
//    @Override
//    public boolean isCredentialsNonExpired() {
//        return true;
//    }
//
//    @Override
//    public boolean isEnabled() {
//        return true;
//    }
}
