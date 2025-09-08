package com.example.Airline_Project.controller;


import com.example.Airline_Project.Service.AnalyticsService;
import com.example.Airline_Project.model.Analytics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping("/daily")
    public ResponseEntity<Analytics> getDailyAnalytics(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Analytics analytics = analyticsService.getAnalyticsForDate(date);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/range")
    public ResponseEntity<List<Analytics>> getAnalyticsRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<Analytics> analytics = analyticsService.getAnalyticsForDateRange(startDate, endDate);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/revenue")
    public ResponseEntity<Double> getTotalRevenue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        double revenue = analyticsService.calculateRevenue(startDate, endDate);
        return ResponseEntity.ok(revenue);
    }
}