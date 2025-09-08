package com.example.Airline_Project.Service;


import com.example.Airline_Project.model.SocialAuth;
import com.example.Airline_Project.model.User;

public interface SocialAuthService {
    SocialAuth createSocialAuth(User user, String provider, String providerId, String email);
    SocialAuth findSocialAuth(String provider, String providerId);
    void unlinkSocialAuth(User user, String provider);
}