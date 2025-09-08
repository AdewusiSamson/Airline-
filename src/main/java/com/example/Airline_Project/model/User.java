package com.example.Airline_Project.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;



@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private twoFactorAuth twoFactorAuth = new twoFactorAuth();
    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password; // This will be hashed, never stored plain text

    private String firstName;
    private String lastName;
    private String loyaltyTier = "STANDARD";
    private Integer milesBalance = 0;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role= Role.ROLE_CUSTOMER;; // ROLE_CUSTOMER, ROLE_ADMIN

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @ToString.Exclude // Prevents circular reference in Lombok's toString()
    private List<Booking> bookings = new ArrayList<>();

    public enum Role {
        ROLE_CUSTOMER, ROLE_ADMIN
    }
}