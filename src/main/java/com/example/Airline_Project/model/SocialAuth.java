package com.example.Airline_Project.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
@Data
@Entity
@Table(name = "social_auth")
public class SocialAuth {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String provider; // GOOGLE, FACEBOOK, etc.
    private String providerId;
    private String email;
    private LocalDateTime createdAt;


}