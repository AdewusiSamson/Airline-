package com.example.Airline_Project.Repository;

import com.example.Airline_Project.model.NewsletterSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NewsletterSubscriptionRepository extends JpaRepository<NewsletterSubscription, Long> {
    Optional<NewsletterSubscription> findByEmail(String email);
    Optional<NewsletterSubscription> findBySubscriptionToken(String token);
}