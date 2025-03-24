package com.example.finalyearproject.DataStore;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Set;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Consumer implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int ConsumerId;

    @NotBlank(message = "First Name cannot be blank")
    private String ConsumerFirstName;
    private String ConsumerLastName;

    @NotNull(message = "UserName cannot be blank")
    private String ConsumerName;
    private String ConsumerPassword;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    @Column(unique = true)
    private String ConsumerEmail;

    @NotNull(message = "Phone number cannot be null")
    @Pattern(regexp = "\\+\\{12}")
    @Column(unique = true)
    private int ConsumerPhone;

    @NotBlank(message = "Address cannot be blank")
    @Size(max = 255, message = "Address cannot exceed 255 characters")
    private String ConsumerAddress;

    @OneToMany( cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private Set<Rating> ConsumerRatings;


    @OneToMany( cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private Set<Donation> ConsumerDonations;

    @OneToMany(mappedBy = "consumer", cascade = CascadeType.ALL)
    @JsonManagedReference
    private Set<Order> ConsumerOrder;

    @OneToMany(mappedBy = "consumer",cascade = CascadeType.ALL)
    @JsonManagedReference
    private Set<DeliveryAddresses> SetOfDeliveryAddress;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getPassword() {
        return this.ConsumerPassword;
    }

    @Override
    public String getUsername() {
        return this.ConsumerName;
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
