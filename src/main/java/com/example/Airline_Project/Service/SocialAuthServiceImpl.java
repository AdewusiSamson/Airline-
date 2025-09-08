package com.example.Airline_Project.Service;

import com.example.Airline_Project.Repository.SocialAuthRepository;
import com.example.Airline_Project.model.SocialAuth;
import com.example.Airline_Project.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SocialAuthServiceImpl implements SocialAuthService {

    @Autowired
    private SocialAuthRepository socialAuthRepository;

    @Override
    public SocialAuth createSocialAuth(User user, String provider, String providerId, String email) {
        SocialAuth socialAuth = new SocialAuth();
        socialAuth.setUser(user);
        socialAuth.setProvider(provider);
        socialAuth.setProviderId(providerId);
        socialAuth.setEmail(email);
        socialAuth.setCreatedAt(LocalDateTime.now());

        return socialAuthRepository.save(socialAuth);
    }

    @Override
    public SocialAuth findSocialAuth(String provider, String providerId) {
        return socialAuthRepository.findByProviderAndProviderId(provider, providerId)
                .orElse(null);
    }
    @Override
    public void unlinkSocialAuth(User user, String provider) {
        socialAuthRepository.findByUserAndProvider(user, provider)
                .ifPresent(socialAuthRepository::delete);
}
}

