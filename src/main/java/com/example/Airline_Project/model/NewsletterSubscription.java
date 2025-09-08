package com.example.Airline_Project.model;



import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
@Entity
@Data
@Table(name = "newsletter_subscriptions")

public class NewsletterSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private boolean active;
    private LocalDateTime subscribedAt;
    private LocalDateTime unsubscribedAt;
    private String subscriptionToken;


}

