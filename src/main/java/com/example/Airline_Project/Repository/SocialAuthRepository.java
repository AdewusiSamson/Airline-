package com.example.Airline_Project.Repository;

import com.example.Airline_Project.model.SocialAuth;
import com.example.Airline_Project.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SocialAuthRepository extends JpaRepository<SocialAuth, Long> {
    Optional<SocialAuth> findByProviderAndProviderId(String provider, String providerId);
    Optional<SocialAuth> findByUserAndProvider(User user, String provider);
}
