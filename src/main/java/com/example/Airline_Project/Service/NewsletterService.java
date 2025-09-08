package com.example.Airline_Project.Service;

import com.example.Airline_Project.model.NewsletterSubscription;

public interface NewsletterService {
    NewsletterSubscription subscribe(String email);
    void unsubscribe(String token);
    void unsubscribeByEmail(String email);
    boolean isSubscribed(String email);
}