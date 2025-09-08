package com.example.Airline_Project.Service;

import com.example.Airline_Project.Repository.NewsletterSubscriptionRepository;
import com.example.Airline_Project.model.NewsletterSubscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class NewsletterServiceImpl implements NewsletterService {

    @Autowired
    private NewsletterSubscriptionRepository newsletterRepository;

    @Override
    public NewsletterSubscription subscribe(String email) {
        NewsletterSubscription subscription = newsletterRepository.findByEmail(email)
                .orElse(new NewsletterSubscription());

        subscription.setEmail(email);
        subscription.setActive(true);
        subscription.setSubscribedAt(LocalDateTime.now());
        subscription.setUnsubscribedAt(null);
        subscription.setSubscriptionToken(UUID.randomUUID().toString());

        return newsletterRepository.save(subscription);
    }

    @Override
    public void unsubscribe(String token) {
        newsletterRepository.findBySubscriptionToken(token)
                .ifPresent(subscription -> {
                    subscription.setActive(false);
                    subscription.setUnsubscribedAt(LocalDateTime.now());
                    newsletterRepository.save(subscription);
                });
    }

    @Override
    public void unsubscribeByEmail(String email) {
        newsletterRepository.findByEmail(email)
                .ifPresent(subscription -> {
                    subscription.setActive(false);
                    subscription.setUnsubscribedAt(LocalDateTime.now());
                    newsletterRepository.save(subscription);
                });
    }

    @Override
    public boolean isSubscribed(String email) {
        return newsletterRepository.findByEmail(email)
                .map(NewsletterSubscription::isActive)
                .orElse(false);
}
}
