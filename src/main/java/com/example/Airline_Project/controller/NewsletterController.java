package com.example.Airline_Project.controller;

import com.example.Airline_Project.Service.NewsletterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/newsletter")
public class NewsletterController {

    @Autowired
    private NewsletterService newsletterService;

    @PostMapping("/subscribe")
    public ResponseEntity<?> subscribe(@RequestParam String email) {
        newsletterService.subscribe(email);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/unsubscribe")
    public ResponseEntity<?> unsubscribe(@RequestParam(required = false) String token,
                                         @RequestParam(required = false) String email) {
        if (token != null) {
            newsletterService.unsubscribe(token);
        } else if (email != null) {
            newsletterService.unsubscribeByEmail(email);
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/status")
    public ResponseEntity<Boolean> checkStatus(@RequestParam String email) {
        boolean isSubscribed = newsletterService.isSubscribed(email);
        return ResponseEntity.ok(isSubscribed);
    }
}